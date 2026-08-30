.class public Lconstructors/TestConstructorFluentBuilderInline;
.super Ljava/lang/Exception;

.field private final time:J

.method public constructor <init>(Ljava/lang/String;J)V
    .locals 2

    invoke-static {p1}, Lconstructors/ConstructorFluentBuilderHelper;->make(Ljava/lang/String;)Ljava/lang/StringBuilder;
    move-result-object v0
    invoke-static {p2, p3}, Lconstructors/ConstructorFluentBuilderHelper;->suffix(J)Ljava/lang/String;
    move-result-object v1
    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
    move-result-object v0
    invoke-direct {p0, v0}, Ljava/lang/Exception;-><init>(Ljava/lang/String;)V
    iput-wide p2, p0, Lconstructors/TestConstructorFluentBuilderInline;->time:J
    return-void
.end method
