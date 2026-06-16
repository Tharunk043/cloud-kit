-keepnames class com.example.data.api.SyncUserRequest
-if class com.example.data.api.SyncUserRequest
-keep class com.example.data.api.SyncUserRequestJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.example.data.api.SyncUserRequest
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-keepclassmembers class com.example.data.api.SyncUserRequest {
    public synthetic <init>(java.lang.String,java.lang.String,java.lang.String,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
