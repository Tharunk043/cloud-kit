-keepnames class com.example.data.api.MongoOrderItem
-if class com.example.data.api.MongoOrderItem
-keep class com.example.data.api.MongoOrderItemJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.example.data.api.MongoOrderItem
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-keepclassmembers class com.example.data.api.MongoOrderItem {
    public synthetic <init>(java.lang.String,java.lang.String,int,double,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
