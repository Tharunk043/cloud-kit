-keepnames class com.example.data.api.AddWalletTxRequest
-if class com.example.data.api.AddWalletTxRequest
-keep class com.example.data.api.AddWalletTxRequestJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.example.data.api.AddWalletTxRequest
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-keepclassmembers class com.example.data.api.AddWalletTxRequest {
    public synthetic <init>(java.lang.String,double,java.lang.String,java.lang.String,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
