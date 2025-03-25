package com.example.supabaselib

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {
    private val client = createSupabaseClient(
        supabaseUrl = "https://lztmkheehgluuijdgoob.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imx6dG1raGVlaGdsdXVpamRnb29iIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDI4MjIxNjEsImV4cCI6MjA1ODM5ODE2MX0.RWkarL4KI0YWY3YeD5FNYxdCWdcpl79j9SQdzLg5ha0"
    ) {
        install(Postgrest)
    }

    fun getClient(): SupabaseClient {
        return client
    }
}
