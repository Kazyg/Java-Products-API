# Item Comparison API

API REST desenvolvida com Spring Boot para gerenciamento e comparação de produtos.

## Executar e testar

Requisitos locais: Java 21 e Maven 3.9+. Alternativamente, use Docker com Compose.

```sh
mvn clean verify
mvn spring-boot:run
```

```sh
docker compose up --build -d
docker compose logs -f api
curl http://localhost:8080/actuator/health
curl http://localhost:8080/products
docker compose down
```

Swagger: http://localhost:8080/swagger-ui/index.html. Contrato OpenAPI gerado: http://localhost:8080/v3/api-docs.

O perfil local usa SQLite em memória e carrega quatro produtos de exemplo. O Compose usa SQLite em arquivo com volume persistente e os mesmos exemplos na primeira inicialização. `docker compose down` preserva os dados; a opção `--volumes` os apaga. IDs são gerados pelo banco: consulte `/products` antes de comparar.

## Contrato HTTP

| Método | Rota | Resultado |
| --- | --- | --- |
| POST | `/products` | Cria produto, `201` sem corpo |
| GET | `/products` | Lista produtos, `200` |
| GET | `/products/{id}` | Consulta produto, `200` ou `404` |
| GET | `/products/search?name=iphone` | Busca parcial sem diferenciar maiúsculas, `200` |
| POST | `/products/compare` | Compara IDs, `200`, `400` ou `404` |
| DELETE | `/products/{id}` | Remove produto, `204` ou `404` |
| DELETE | `/products` | Remove todos, `204` |

Esta versão de portfólio adota `/products` como rota base e `products` como campo da lista na comparação. É uma mudança incompatível com os antigos `/models` e `models`; consumidores devem atualizar suas chamadas. O enunciado original não está disponível, portanto este documento define o contrato desta versão sem afirmar equivalência ao desafio original.

## Operação e observabilidade

- `GET /actuator/health`: saúde geral, incluindo conexão com o banco.
- `GET /actuator/health/liveness`: estado da aplicação, independente do banco.
- `GET /actuator/health/readiness`: disponibilidade para receber tráfego e conexão com o banco.
- Somente health é exposto pelo Actuator; detalhes internos não aparecem na resposta. Estado saudável retorna `200` com `{"status":"UP"}`; falha de saúde retorna `503`.
- Logs no console registram método, rota, status, duração e um UUID por requisição. O header `X-Request-ID` permite localizar o atendimento nos logs. Corpos, query strings e credenciais não são registrados pelo filtro.
- Exceções inesperadas têm stack trace no servidor e mensagem genérica na resposta. Encerramento gracioso permite até 20 segundos para requisições em andamento.
- Dockerfile com compilação e testes em estágio separado, runtime Java 21, usuário sem privilégios e health check. GitHub Actions executa `mvn verify` e constrói a imagem.

Configuração dos probes baseada na [documentação do Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/3.2.x/reference/html/actuator.html).

| Configuração | Padrão | Uso |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | local, sem perfil explícito | `container` ativa SQLite persistente em `/app/data` |
| `APP_SEED_DATA` | `true` local; `false` no perfil container | O Compose define `true` para demonstração; só insere exemplos se a tabela estiver vazia |
| `APP_LOG_LEVEL` | `INFO` | Nível de logs da aplicação |
| `SERVER_PORT` | `8080` | Porta HTTP; ao alterar no container, ajuste também o health check e o mapeamento de porta |

## Arquitetura e limites

Controllers recebem DTOs validados, services concentram as regras e transações, e repositories acessam SQLite via Spring Data JPA. A comparação usa `BigDecimal` para preços e arredonda a média para duas casas decimais. Testes unitários validam regras; testes de integração exercitam contrato HTTP, validações e observabilidade.

O projeto demonstra práticas operacionais, mas não pretende ser uma plataforma pronta para qualquer produção. SQLite e um pool de conexão unitário atendem uma instância; escalar horizontalmente exige revisar o banco. O perfil container usa `ddl-auto=update` para a demonstração; ambientes reais devem ter migrações versionadas. A API não possui autenticação, autorização nem paginação, inclusive nas rotas de exclusão. O Compose publica a porta apenas no localhost. Antes de exposição pública, esses controles e a atualização das dependências precisam fazer parte do escopo de implantação.

## Visão geral

A API permite:

- cadastrar produtos
- listar produtos
- buscar produto por id
- buscar produtos por nome parcial
- comparar produtos por uma lista de ids
- remover um produto específico
- remover todos os produtos

A rota base da API é:

```text
/products
```

## Padrão de erro

Quando ocorre erro tratado pela aplicação, a resposta segue o formato:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "name: name must not be blank",
  "timestamp": "2026-04-27T15:00:00"
}
```

Campos:

- `status`: código HTTP
- `error`: descrição do status HTTP
- `message`: detalhe do erro
- `timestamp`: data e hora do erro

## Endpoint: criar produto

- **Método**: `POST`
- **Rota**: `/products`
- **Lógica**: cria um novo produto a partir dos dados enviados no body. O `id` não deve ser enviado, pois é gerado automaticamente pelo banco.

### Request body

```json
{
  "name": "iPhone 15",
  "urlImage": "https://example.com/images/iphone15.png",
  "description": "Smartphone Apple com tela Super Retina XDR e chip A16 Bionic.",
  "price": 5299.00,
  "rating": 5,
  "specifications": "128GB, 6GB RAM, Câmera 48MP"
}
```

### Validações

- `name` não pode estar em branco
- `description` não pode estar em branco
- `specifications` não pode estar em branco
- `price` deve ser maior que `0` e ter até 2 casas decimais
- `rating` deve estar entre `1` e `5`

### Possíveis respostas

- **`201 Created`**
  - produto criado com sucesso
  - sem body

- **`400 Bad Request`**
  - body inválido
  - body malformado

### Exemplo de erro `400`

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "name: name must not be blank, price: price must be greater than 0",
  "timestamp": "2026-04-27T15:00:00"
}
```

## Endpoint: listar todos os produtos

- **Método**: `GET`
- **Rota**: `/products`
- **Lógica**: retorna todos os produtos cadastrados, convertidos para DTO de resposta.

### Request body

- não possui

### Possíveis respostas

- **`200 OK`**

### Exemplo de resposta

```json
[
  {
    "id": 1,
    "name": "iPhone 15",
    "urlImage": "https://example.com/images/iphone15.png",
    "description": "Smartphone Apple com tela Super Retina XDR e chip A16 Bionic.",
    "price": 5299.00,
    "rating": 5
  }
]
```

## Endpoint: buscar produto por id

- **Método**: `GET`
- **Rota**: `/products/{id}`
- **Lógica**: retorna um produto específico pelo `id`. Se o produto não existir, retorna erro `404`.

### Request body

- não possui

### Parâmetro de rota

- `id`: identificador do produto

### Possíveis respostas

- **`200 OK`**

```json
{
  "id": 1,
  "name": "iPhone 15",
  "urlImage": "https://example.com/images/iphone15.png",
  "description": "Smartphone Apple com tela Super Retina XDR e chip A16 Bionic.",
  "price": 5299.00,
  "rating": 5
}
```

- **`404 Not Found`**

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "No product with given id found.",
  "timestamp": "2026-04-27T15:00:00"
}
```

## Endpoint: buscar produtos por nome

- **Método**: `GET`
- **Rota**: `/products/search?name={name}`
- **Lógica**: busca produtos por correspondência parcial no nome, ignorando maiúsculas e minúsculas.

### Request body

- não possui

### Query param

- `name`: trecho do nome do produto

### Exemplo

```text
/products/search?name=iphone
```

### Possíveis respostas

- **`200 OK`**

```json
[
  {
    "id": 1,
    "name": "iPhone 15",
    "urlImage": "https://example.com/images/iphone15.png",
    "description": "Smartphone Apple com tela Super Retina XDR e chip A16 Bionic.",
    "price": 5299.00,
    "rating": 5
  }
]
```

## Endpoint: comparar produtos

- **Método**: `POST`
- **Rota**: `/products/compare`
- **Lógica**: recebe uma lista de ids, busca os produtos correspondentes e retorna tanto os produtos encontrados quanto métricas agregadas de preço e avaliação.

### Request body

```json
{
  "ids": [1, 2, 3]
}
```

### Validações

- `ids` não pode estar vazio
- `ids` não pode conter valores nulos

### Possíveis respostas

- **`200 OK`**

```json
{
  "products": [
    {
      "id": 1,
      "name": "iPhone 15",
      "urlImage": "https://example.com/images/iphone15.png",
      "description": "Smartphone Apple com tela Super Retina XDR e chip A16 Bionic.",
      "price": 5299.00,
      "rating": 5
    },
    {
      "id": 2,
      "name": "Galaxy S24",
      "urlImage": "https://example.com/images/galaxys24.png",
      "description": "Smartphone Samsung com Galaxy AI e tela AMOLED 120Hz.",
      "price": 4699.00,
      "rating": 4
    }
  ],
  "lowestPrice": 4699.00,
  "highestPrice": 5299.00,
  "averagePrice": 4999.00,
  "lowestRating": 4,
  "highestRating": 5,
  "averageRating": 4.5
}
```

- **`400 Bad Request`**
  - lista de ids vazia
  - lista com valores nulos
  - body malformado

- **`404 Not Found`**
  - nenhum produto encontrado para os ids enviados
  - um ou mais ids informados não existem

### Exemplo de erro `404`

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "One or more informed ids were not found.",
  "timestamp": "2026-04-27T15:00:00"
}
```

## Endpoint: deletar produto por id

- **Método**: `DELETE`
- **Rota**: `/products/{id}`
- **Lógica**: remove um produto específico pelo `id`. Se o produto não existir, retorna `404`.

### Request body

- não possui

### Parâmetro de rota

- `id`: identificador do produto

### Possíveis respostas

- **`204 No Content`**
  - produto removido com sucesso
  - sem body

- **`404 Not Found`**

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "No resource found for the provided id.",
  "timestamp": "2026-04-27T15:00:00"
}
```

## Endpoint: deletar todos os produtos

- **Método**: `DELETE`
- **Rota**: `/products`
- **Lógica**: remove todos os produtos cadastrados.

### Request body

- não possui

### Possíveis respostas

- **`204 No Content`**
  - todos os produtos foram removidos
  - sem body

## DTOs principais

### `CreateModelRequest`

Usado no endpoint de criação.

```json
{
  "name": "string",
  "urlImage": "string",
  "description": "string",
  "price": 1.00,
  "rating": 5,
  "specifications": "string"
}
```

### `ModelResponseDto`

Usado nos endpoints de consulta.

```json
{
  "id": 1,
  "name": "string",
  "urlImage": "string",
  "description": "string",
  "price": 1.00,
  "rating": 5
}
```

### `ModelComparisonRequest`

```json
{
  "ids": [1, 2, 3]
}
```

### `ModelComparisonResponse`

```json
{
  "products": [],
  "lowestPrice": 0.00,
  "highestPrice": 0.00,
  "averagePrice": 0.00,
  "lowestRating": 0,
  "highestRating": 0,
  "averageRating": 0.0
}
```

## Swagger

Se a aplicação estiver rodando localmente, a documentação interativa pode ser acessada em:

```text
http://localhost:8080/swagger-ui/index.html
```

## Resumo da lógica da API

- o cadastro usa DTO de entrada sem `id`
- o `id` é gerado automaticamente pelo banco
- as consultas retornam DTOs de resposta
- a busca por nome usa correspondência parcial com ignore case
- a comparação calcula estatísticas agregadas com base nos ids informados
- os erros são centralizados em um `GlobalExceptionHandler`
- validações inválidas retornam `400 Bad Request`
- recursos inexistentes retornam `404 Not Found`
