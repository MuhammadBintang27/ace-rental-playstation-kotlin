package com.ace.playstation.auth

import android.util.Log
import com.ace.playstation.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest


object SupabaseClientInstance {
    private val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY
    ) {
        install(Postgrest)
        install(Auth){
            alwaysAutoRefresh=true
        }
    }

    fun getClient(): SupabaseClient {
        Log.d("tes client", "getClient: $client")
        return client
    }
}
