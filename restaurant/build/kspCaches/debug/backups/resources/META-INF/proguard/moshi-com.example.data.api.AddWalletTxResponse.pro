-keepnames class com.example.data.api.AddWalletTxResponse
-if class com.example.data.api.AddWalletTxResponse
-keep class com.example.data.api.AddWalletTxResponseJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
