-keepnames class com.example.data.api.MongoUser
-if class com.example.data.api.MongoUser
-keep class com.example.data.api.MongoUserJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.example.data.api.MongoUser
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-keepclassmembers class com.example.data.api.MongoUser {
    public synthetic <init>(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,boolean,int,boolean,double,long,long,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
