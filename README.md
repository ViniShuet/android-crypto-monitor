## Autor
Vinicius Ferreira Shuet - RM:98160

## Descrição
Este projeto foi criado com o intuito de exibir a cotação atual do Bitcoin. Os usuários podem atualizar as informações a qualquer momento clicando no botão "Atualizar".

## Api utilizada
  mercadoBitcoin.net	

## Funcionalidades do Código - MainActivity.kt

### Configuração da Toolbar
A `MainActivity` contém uma função chamada `configureToolbar`, que personaliza a barra de ferramentas (toolbar) do aplicativo:
- Define um título para o aplicativo com base no recurso `R.string.app_title`.
- Ajusta a cor do texto e o plano de fundo da toolbar.
```kotlin
val toolbarMain: Toolbar = findViewById(R.id.toolbar_main)
configureToolbar(toolbarMain)

  private fun configureToolbar(toolbar: Toolbar) {
        setSupportActionBar(toolbar)
        toolbar.setTitleTextColor(getColor(R.color.white))
        supportActionBar?.setTitle(getText(R.string.app_title))
        supportActionBar?.setBackgroundDrawable(getDrawable(R.color.primary))
    }
```


### Botão de Atualização
O botão "Atualizar" (`btn_refresh`) permite que os usuários recarreguem a cotação do Bitcoin manualmente. Ele está configurado para chamar a função `makeRestCall` sempre que for clicado.
```kotlin
val btnRefresh: Button = findViewById(R.id.btn_refresh)
btnRefresh.setOnClickListener {
    makeRestCall()
}
```
#### Como Funciona
- **Função Principal**: Ao clicar no botão, a função `makeRestCall` é executada, que realiza uma chamada à API do Mercado Bitcoin para obter a cotação mais recente.
- **Atualização Dinâmica**: Os dados da cotação e a data/hora da última atualização são exibidos diretamente na interface do usuário após a chamada bem-sucedida.

### Chamadas REST para a API do Mercado Bitcoin
A função `makeRestCall` utiliza corrotinas (`CoroutineScope`) para realizar chamadas assíncronas à API do Mercado Bitcoin. Ela gerencia a atualização da interface do usuário com os dados mais recentes ou exibe mensagens de erro em caso de falha.
```kotlin
private fun makeRestCall() {
    CoroutineScope(Dispatchers.Main).launch {
        try {
            val service = MercadoBitcoinServiceFactory().create()
            val response = service.getTicker()

            if (response.isSuccessful) {
                val tickerResponse = response.body()

                val lblValue: TextView = findViewById(R.id.lbl_value)
                val lblDate: TextView = findViewById(R.id.lbl_date)

                val lastValue = tickerResponse?.ticker?.last?.toDoubleOrNull()
                if (lastValue != null) {
                    val numberFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
                    lblValue.text = numberFormat.format(lastValue)
                }

                val date = tickerResponse?.ticker?.date?.let { Date(it * 1000L) }
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                lblDate.text = sdf.format(date)

            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Bad Request"
                    401 -> "Unauthorized"
                    403 -> "Forbidden"
                    404 -> "Not Found"
                    else -> "Unknown error"
                }
                Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_LONG).show()
            }

        } catch (e: Exception) {
            Toast.makeText(this@MainActivity, "Falha na chamada: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
```

- **Chamada à API**: A função utiliza um serviço gerado pela `MercadoBitcoinServiceFactory` para obter os dados da API.
- **Atualização da Interface**: Os valores recebidos são formatados e exibidos em dois componentes `TextView`:
  - `lblValue`: Mostra a cotação do Bitcoin formatada como moeda brasileira (R$).
  - `lblDate`: Mostra a data e hora da última atualização em formato legível (`dd/MM/yyyy HH:mm:ss`).
- **Tratamento de Respostas**:
  - Respostas bem-sucedidas (`response.isSuccessful`) atualizam os componentes da interface.
  - Erros de resposta (como `404 - Not Found`) são tratados exibindo mensagens específicas.
- **Tratamento de Exceções**: Exibe mensagens de erro no caso de falhas na chamada à API.

Essas funcionalidades tornam o aplicativo confiável e fácil de usar, permitindo que os usuários acompanhem a cotação do Bitcoin de forma prática e eficiente.

---

## Funcionalidades do Código - TicketResponse.kt

### Estrutura de Dados: TickerResponse e Ticker
O aplicativo utiliza duas classes principais, `TickerResponse` e `Ticker`, para representar os dados retornados pela API do Mercado Bitcoin.

#### Estrutura das Classes
Aqui está a definição dessas classes:

```kotlin
class TickerResponse(
    val ticker: Ticker
) 

class Ticker(
    val high: String,
    val low: String,
    val vol: String,
    val last: String,
    val buy: String,
    val sell: String,
    val date: Long
) 
```

#### Explicação
- **TickerResponse**: 
  - Esta classe encapsula a resposta da API e contém um único atributo:
    - `ticker`: Uma instância da classe `Ticker`, que contém os detalhes da cotação.

- **Ticker**:
  - Esta classe contém os detalhes da cotação retornados pela API. Os atributos incluem:
    - `high`: O valor mais alto da cotação no período.
    - `low`: O valor mais baixo da cotação no período.
    - `vol`: O volume negociado no período.
    - `last`: O último valor registrado da cotação.
    - `buy`: O valor de compra mais recente.
    - `sell`: O valor de venda mais recente.
    - `date`: Um timestamp representando a data e hora da atualização.

#### Propósito
Essas classes são projetadas para mapear os dados JSON retornados pela API do Mercado Bitcoin, facilitando o acesso e manipulação das informações no aplicativo.

---

## Funcionalidades do Código - MercadoBitcoinService.kt

### Serviço de Conexão com a API: MercadoBitcoinService
O aplicativo utiliza a interface `MercadoBitcoinService` para se conectar à API do Mercado Bitcoin e obter os dados da cotação.

#### Código da Interface
Abaixo está a definição da interface:

```kotlin
import retrofit2.Response
import retrofit2.http.GET

interface MercadoBitcoinService {

    @GET("api/BTC/ticker/")
    suspend fun getTicker(): Response<TickerResponse>
}
```

#### Explicação
- **Retrofit**: A interface utiliza o Retrofit, uma biblioteca popular para chamadas HTTP em Android.
- **Função `getTicker`**:
  - Método HTTP: `GET`.
  - Endpoint: `api/BTC/ticker/`.
  - Retorno: Um objeto `Response` contendo uma instância de `TickerResponse`.
  - `suspend`: A função é declarada como uma função suspensa para ser usada em corrotinas, permitindo chamadas assíncronas sem bloquear a thread principal.

#### Como Funciona
1. A interface é implementada com a ajuda do Retrofit.
2. Quando o método `getTicker` é chamado, uma solicitação HTTP GET é feita ao endpoint especificado.
3. Os dados retornados pela API são mapeados para a classe `TickerResponse`.

#### Uso no Aplicativo
A interface é utilizada na função `makeRestCall` da `MainActivity`, onde:
- Uma instância de `MercadoBitcoinService` é criada por meio da `MercadoBitcoinServiceFactory`.
- A função `getTicker` é chamada para buscar os dados da API.
- Os dados retornados são utilizados para atualizar a interface do usuário.


Essa interface é fundamental para o funcionamento do aplicativo, pois gerencia a comunicação com a API do Mercado Bitcoin.

Essa estrutura de dados é essencial para o funcionamento do aplicativo, pois organiza e simplifica o acesso às informações da API.

---

## Funcionalidades do Código - MercadoBitcoinServiceFactory.kt

### Fábrica de Serviços: MercadoBitcoinServiceFactory
A classe `MercadoBitcoinServiceFactory` é responsável por criar instâncias da interface `MercadoBitcoinService` para se comunicar com a API do Mercado Bitcoin.

#### Código da Classe
Abaixo está a definição da classe:

```kotlin

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MercadoBitcoinServiceFactory {

    fun create(): MercadoBitcoinService {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.mercadobitcoin.net/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(MercadoBitcoinService::class.java)
    }
}
```

#### Explicação
- **Objetivo da Classe**:
  - É uma "fábrica" para criar instâncias da interface `MercadoBitcoinService`, encapsulando a configuração do Retrofit.
  - Desacopla a lógica de criação de serviços Retrofit do restante do aplicativo, tornando o código mais modular e reutilizável.

- **Configuração do Retrofit**:
  - `baseUrl`: Define a URL base da API (`https://www.mercadobitcoin.net/`).
  - `addConverterFactory`: Usa o `GsonConverterFactory` para converter automaticamente as respostas JSON da API em objetos Kotlin.

- **Método `create`**:
  - Retorna uma instância de `MercadoBitcoinService` configurada para se comunicar com a API do Mercado Bitcoin.

#### Como Funciona no Aplicativo
1. A classe é utilizada na `MainActivity` através do método `create` para obter uma instância de `MercadoBitcoinService`.
2. Essa instância é usada para chamar a função `getTicker`, que faz a solicitação à API e retorna os dados necessários.

#### Benefícios
- **Modularidade**: Centraliza a lógica de criação e configuração do Retrofit, facilitando futuras alterações, como mudar a URL base ou adicionar interceptadores.
- **Reutilização**: Permite que outros componentes do aplicativo reutilizem o mesmo serviço sem duplicar código.

A `MercadoBitcoinServiceFactory` é essencial para estabelecer a conexão com a API e garantir que o Retrofit esteja configurado corretamente.

---

### Dependência Adicionada: kotlinx-coroutines-android - build.gradle.kts
O projeto utiliza a biblioteca `kotlinx-coroutines-android` para lidar com operações assíncronas no Android de maneira eficiente, sem bloquear a interface do usuário.

#### Dependência Adicionada
No arquivo `build.gradle.kts`, foi adicionada a seguinte linha:

```kotlin
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.2")
```

#### Explicação
- **`kotlinx-coroutines-android`**: 
  - Esta biblioteca faz parte do Kotlin Coroutines e é otimizada para o ambiente Android.
  - Permite a utilização de corrotinas no Android para lidar com tarefas assíncronas, como chamadas de rede, sem bloquear a `MainThread`.

- **Versão**: 
  - A versão utilizada é a `1.5.2`. É importante manter as dependências atualizadas para aproveitar as melhorias de desempenho e correções de bugs.

#### Como é Usada no Projeto
- A biblioteca é utilizada na função `makeRestCall` da `MainActivity`:
  - A função é marcada como `suspend` e executada dentro do escopo principal (`Dispatchers.Main`).
  - Isso garante que as operações assíncronas, como chamadas para a API, sejam realizadas fora da thread principal, enquanto as atualizações da interface do usuário continuam sendo feitas na `MainThread`.

