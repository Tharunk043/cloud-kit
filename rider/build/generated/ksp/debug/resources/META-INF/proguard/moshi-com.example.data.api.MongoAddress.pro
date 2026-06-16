-keepnames class com.example.data.api.MongoAddress
-if class com.example.data.api.MongoAddress
-keep class com.example.data.api.MongoAddressJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
