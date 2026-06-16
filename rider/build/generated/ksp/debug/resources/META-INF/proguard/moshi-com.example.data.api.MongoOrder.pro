-keepnames class com.example.data.api.MongoOrder
-if class com.example.data.api.MongoOrder
-keep class com.example.data.api.MongoOrderJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.example.data.api.MongoOrder
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-keepclassmembers class com.example.data.api.MongoOrder {
    public synthetic <init>(java.lang.String,java.lang.String,java.lang.String,java.util.List,double,java.lang.String,java.lang.String,java.lang.String,double,double,double,double,double,double,long,float,java.lang.String,java.lang.String,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
