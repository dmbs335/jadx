.class public Lconstructors/TestConstructorArrayFactoryBlocked;
.super Lconstructors/TestConstructorArrayFactoryBlockedParent;

.method public constructor <init>(Ljava/lang/String;)V
    .locals 1

    invoke-static {p1}, Lconstructors/TestConstructorArrayFactoryBlockedHelper;->make(Ljava/lang/String;)[Ljava/lang/Object;
    move-result-object v0
    invoke-direct {p0, v0, v0}, Lconstructors/TestConstructorArrayFactoryBlockedParent;-><init>([Ljava/lang/Object;[Ljava/lang/Object;)V
    return-void
.end method
