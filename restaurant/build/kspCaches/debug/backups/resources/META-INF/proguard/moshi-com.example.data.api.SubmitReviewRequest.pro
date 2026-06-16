-keepnames class com.example.data.api.SubmitReviewRequest
-if class com.example.data.api.SubmitReviewRequest
-keep class com.example.data.api.SubmitReviewRequestJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
