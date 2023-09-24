package studio1a23.lyricovaJukebox.data

import com.apollographql.apollo3.ApolloClient

// const val SERVER_ROOT = "https://jukebox.1a23.studio"
const val SERVER_ROOT = "http://10.0.0.212:3000"

val apolloClient = ApolloClient.Builder()
    .serverUrl("$SERVER_ROOT/graphql")
    .build()