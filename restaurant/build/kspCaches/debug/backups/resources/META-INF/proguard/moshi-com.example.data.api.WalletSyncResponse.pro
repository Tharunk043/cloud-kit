-keepnames class com.example.data.api.WalletSyncResponse
-if class com.example.data.api.WalletSyncResponse
-keep class com.example.data.api.WalletSyncResponseJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
