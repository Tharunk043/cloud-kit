-keepnames class com.example.data.api.PlaceOrderRequest
-if class com.example.data.api.PlaceOrderRequest
-keep class com.example.data.api.PlaceOrderRequestJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.example.data.api.PlaceOrderRequest
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-keepclassmembers class com.example.data.api.PlaceOrderRequest {
    public synthetic <init>(java.lang.String,java.lang.String,java.util.List,double,java.lang.String,java.lang.String,double,double,double,double,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
