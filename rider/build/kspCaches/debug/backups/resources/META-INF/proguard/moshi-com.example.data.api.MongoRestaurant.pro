-keepnames class com.example.data.api.MongoRestaurant
-if class com.example.data.api.MongoRestaurant
-keep class com.example.data.api.MongoRestaurantJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.example.data.api.MongoRestaurant
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-keepclassmembers class com.example.data.api.MongoRestaurant {
    public synthetic <init>(java.lang.String,java.lang.String,java.lang.String,float,int,double,java.lang.String,java.lang.String,double,double,boolean,boolean,java.lang.String,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
