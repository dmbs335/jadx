.class public Lconstructors/TestConstructorMoveWarning;
.super Lconstructors/TestConstructorMoveWarningParent;

.field private final value:Ljava/lang/Object;

.method public constructor <init>(Ljava/lang/Object;I)V
    .locals 0

    iput-object p1, p0, Lconstructors/TestConstructorMoveWarning;->value:Ljava/lang/Object;
    invoke-direct {p0, p2}, Lconstructors/TestConstructorMoveWarningParent;-><init>(I)V
    return-void
.end method
