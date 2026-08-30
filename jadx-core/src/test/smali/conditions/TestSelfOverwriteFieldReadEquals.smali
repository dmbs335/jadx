.class public final Lconditions/TestSelfOverwriteFieldReadEquals;
.super Ljava/lang/Object;

.field private final timestamp:J
.field private final value:Ljava/lang/Object;

.method public constructor <init>(JLjava/lang/Object;)V
    .registers 5
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    iput-wide p1, p0, Lconditions/TestSelfOverwriteFieldReadEquals;->timestamp:J
    iput-object p3, p0, Lconditions/TestSelfOverwriteFieldReadEquals;->value:Ljava/lang/Object;
    return-void
.end method

.method public final equals(Ljava/lang/Object;)Z
    .registers 8
    if-ne p0, p1, :not_same
    const/4 v0, 0x1
    return v0

    :not_same
    if-eqz p1, :not_equal
    instance-of v0, p1, Lconditions/TestSelfOverwriteFieldReadEquals;
    if-eqz v0, :not_equal
    check-cast p1, Lconditions/TestSelfOverwriteFieldReadEquals;
    iget-wide v0, p0, Lconditions/TestSelfOverwriteFieldReadEquals;->timestamp:J
    iget-wide v2, p1, Lconditions/TestSelfOverwriteFieldReadEquals;->timestamp:J
    cmp-long v0, v0, v2
    if-nez v0, :not_equal

    # R8 commonly reuses the object parameter register for the compared field value.
    iget-object p1, p1, Lconditions/TestSelfOverwriteFieldReadEquals;->value:Ljava/lang/Object;
    iget-object v2, p0, Lconditions/TestSelfOverwriteFieldReadEquals;->value:Ljava/lang/Object;
    if-eq v2, p1, :equal
    if-eqz v2, :not_equal
    invoke-virtual {v2, p1}, Ljava/lang/Object;->equals(Ljava/lang/Object;)Z
    move-result v0
    if-eqz v0, :not_equal

    :equal
    const/4 v0, 0x1
    return v0

    :not_equal
    const/4 v0, 0x0
    return v0
.end method
