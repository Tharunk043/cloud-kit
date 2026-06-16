-keepnames class com.example.data.api.ApiResponse
-if class com.example.data.api.ApiResponse
-keep class com.example.data.api.ApiResponseJsonAdapter {
    public <init>(com.squareup.moshi.Moshi,java.lang.reflect.Type[]);
}
-if class com.example.data.api.ApiResponse
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-keepclassmembers class com.example.data.api.ApiResponse {
    public synthetic <init>(boolean,java.lang.Object,java.lang.String,boolean,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
