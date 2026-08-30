.class public Lconstructors/TestConstructorObjectValueOfArgBlocked;
.super Ljava/io/IOException;

.method public constructor <init>(Ljava/lang/Object;)V
    .locals 3

    invoke-static {p1}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;
    move-result-object v0
    invoke-virtual {v0}, Ljava/lang/String;->length()I
    move-result v1
    const-string v2, "prefix: "
    if-eqz v1, :empty
    invoke-virtual {v2, v0}, Ljava/lang/String;->concat(Ljava/lang/String;)Ljava/lang/String;
    move-result-object v0
    goto :join

    :empty
    new-instance v0, Ljava/lang/String;
    invoke-direct {v0, v2}, Ljava/lang/String;-><init>(Ljava/lang/String;)V

    :join
    invoke-direct {p0, v0}, Ljava/io/IOException;-><init>(Ljava/lang/String;)V
    return-void
.end method
