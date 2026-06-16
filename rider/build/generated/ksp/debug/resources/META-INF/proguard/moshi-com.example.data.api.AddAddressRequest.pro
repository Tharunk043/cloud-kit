-keepnames class com.example.data.api.AddAddressRequest
-if class com.example.data.api.AddAddressRequest
-keep class com.example.data.api.AddAddressRequestJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
