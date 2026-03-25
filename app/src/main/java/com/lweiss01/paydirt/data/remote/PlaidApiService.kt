package com.lweiss01.paydirt.data.remote

import com.lweiss01.paydirt.domain.model.PlaidLiabilityData
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * PlaidApiService — Retrofit interface to YOUR backend.
 *
 * Your backend is a thin proxy that holds the Plaid client_secret.
 * The Android app never sees the client_secret — only the encrypted
 * access_token after exchange.
 *
 * Recommended backend: Cloudflare Workers (free tier) or Vercel
 * serverless functions. ~50 lines of Node/TypeScript total.
 *
 * Base URL: https://api.paydirt.cash (or your own domain)
 */
interface PlaidApiService {

    /**
     * POST /plaid/link-token
     * Backend calls Plaid /link/token/create and returns the link_token.
     */
    @POST("plaid/link-token")
    suspend fun createLinkToken(
        @Body userId: String
    ): LinkTokenResponse

    /**
     * POST /plaid/exchange-token
     * Backend exchanges public_token for access_token.
     * Returns encrypted access_token (never the raw token).
     */
    @POST("plaid/exchange-token")
    suspend fun exchangeToken(
        @Body request: ExchangeTokenRequest
    ): ExchangeTokenResponse

    /**
     * POST /plaid/refresh
     * Backend calls Plaid /liabilities/get with access_token.
     * Returns normalized liability data for all credit cards.
     */
    @POST("plaid/refresh")
    suspend fun refreshAccounts(
        @Body request: RefreshAccountsRequest
    ): RefreshAccountsResponse

    /**
     * POST /plaid/unlink/{itemId}
     * Backend calls Plaid /item/remove to revoke access.
     */
    @POST("plaid/unlink/{itemId}")
    suspend fun unlinkItem(
        @Path("itemId") itemId: String
    )
}

// ─── Request models ───────────────────────────────────────────────────────────

data class ExchangeTokenRequest(
    val publicToken: String,
    val institutionId: String,
    val institutionName: String,
    val accountIds: List<String>,
)

data class RefreshAccountsRequest(
    val encryptedAccessToken: String,
    val itemId: String,
)

// ─── Response models ──────────────────────────────────────────────────────────

data class LinkTokenResponse(
    val linkToken: String,
    val expiration: String,
)

data class ExchangeTokenResponse(
    val itemId: String,
    val encryptedAccessToken: String,
    val institutionId: String,
)

data class RefreshAccountsResponse(
    val itemId: String,
    val liabilities: List<PlaidLiabilityData>,
    val refreshedAt: Long,
)
