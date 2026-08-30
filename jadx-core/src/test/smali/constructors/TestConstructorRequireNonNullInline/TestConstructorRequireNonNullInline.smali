.class public Lconstructors/TestConstructorRequireNonNullInline;
.super Ljava/lang/Object;

.field private final length:I

.method private constructor <init>(I)V
    .locals 0

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    iput p1, p0, Lconstructors/TestConstructorRequireNonNullInline;->length:I
    return-void
.end method

.method public constructor <init>(Ljava/lang/String;)V
    .locals 2

    const-string v0, "value"
    invoke-static {p1, v0}, Ljava/util/Objects;->requireNonNull(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;
    invoke-virtual {p1}, Ljava/lang/String;->length()I
    move-result v1
    invoke-direct {p0, v1}, Lconstructors/TestConstructorRequireNonNullInline;-><init>(I)V
    return-void
.end method
