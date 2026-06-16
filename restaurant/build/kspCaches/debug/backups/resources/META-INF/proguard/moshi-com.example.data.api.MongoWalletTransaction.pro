-keepnames class com.example.data.api.MongoWalletTransaction
-if class com.example.data.api.MongoWalletTransaction
-keep class com.example.data.api.MongoWalletTransactionJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.example.data.api.MongoWalletTransaction
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-keepclassmembers class com.example.data.api.MongoWalletTransaction {
    public synthetic <init>(java.lang.String,double,java.lang.String,long,java.lang.String,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
