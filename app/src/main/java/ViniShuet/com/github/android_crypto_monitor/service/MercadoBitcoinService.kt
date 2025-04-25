package ViniShuet.com.github.android_crypto_monitor.service

import ViniShuet.com.github.android_crypto_monitor.TickerResponse
import retrofit2.Response
import retrofit2.http.GET

interface MercadoBitcoinService {

    @GET("api/BTC/ticker/")
    suspend fun getTicker(): Response<TickerResponse>
}