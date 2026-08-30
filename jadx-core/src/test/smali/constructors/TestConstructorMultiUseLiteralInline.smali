.class public Lconstructors/TestConstructorMultiUseLiteralInline;
.super Ljava/lang/Object;

.field private value:Ljava/lang/Object;

.method public constructor <init>()V
    .locals 1

    const/4 v0, 0x0
    invoke-direct {p0, v0}, Lconstructors/TestConstructorMultiUseLiteralInline;-><init>(Ljava/lang/Object;)V
    iput-object v0, p0, Lconstructors/TestConstructorMultiUseLiteralInline;->value:Ljava/lang/Object;
    return-void
.end method

.method public constructor <init>(Ljava/lang/Object;)V
    .locals 0

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    iput-object p1, p0, Lconstructors/TestConstructorMultiUseLiteralInline;->value:Ljava/lang/Object;
    return-void
.end method
