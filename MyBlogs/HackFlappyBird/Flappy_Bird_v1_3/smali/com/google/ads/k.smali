.class public Lcom/google/ads/k;
.super Ljava/lang/Object;


# instance fields
.field private final a:Ljava/lang/String;

.field private final b:Ljava/lang/String;

.field private final c:Ljava/util/List;

.field private final d:Ljava/util/List;

.field private final e:Ljava/util/HashMap;


# direct methods
.method public constructor <init>(Ljava/lang/String;Ljava/lang/String;Ljava/util/List;Ljava/util/List;Ljava/util/HashMap;)V
    .locals 0

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    invoke-static {p2}, Lcom/google/ads/util/e;->a(Ljava/lang/String;)V

    if-eqz p1, :cond_0

    invoke-static {p1}, Lcom/google/ads/util/e;->a(Ljava/lang/String;)V

    :cond_0
    iput-object p1, p0, Lcom/google/ads/k;->a:Ljava/lang/String;

    iput-object p2, p0, Lcom/google/ads/k;->b:Ljava/lang/String;

    iput-object p3, p0, Lcom/google/ads/k;->c:Ljava/util/List;

    iput-object p5, p0, Lcom/google/ads/k;->e:Ljava/util/HashMap;

    iput-object p4, p0, Lcom/google/ads/k;->d:Ljava/util/List;

    return-void
.end method


# virtual methods
.method public a()Ljava/lang/String;
    .locals 1

    iget-object v0, p0, Lcom/google/ads/k;->a:Ljava/lang/String;

    return-object v0
.end method

.method public b()Ljava/lang/String;
    .locals 1

    iget-object v0, p0, Lcom/google/ads/k;->b:Ljava/lang/String;

    return-object v0
.end method

.method public c()Ljava/util/List;
    .locals 1

    iget-object v0, p0, Lcom/google/ads/k;->c:Ljava/util/List;

    return-object v0
.end method

.method public d()Ljava/util/List;
    .locals 1

    iget-object v0, p0, Lcom/google/ads/k;->d:Ljava/util/List;

    return-object v0
.end method

.method public e()Ljava/util/HashMap;
    .locals 1

    iget-object v0, p0, Lcom/google/ads/k;->e:Ljava/util/HashMap;

    return-object v0
.end method
