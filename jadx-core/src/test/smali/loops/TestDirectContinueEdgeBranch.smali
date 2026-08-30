.class public Lloops/TestDirectContinueEdgeBranch;
.super Ljava/lang/Object;

.method public static test(Ljava/lang/String;[Ljava/lang/String;)V
    .registers 7

    const/4 v0, 0x0
    const/4 v4, 0x0

    :loop
    invoke-virtual {p0}, Ljava/lang/String;->length()I
    move-result v1
    if-ge v0, v1, :return

    invoke-virtual {p0, v0}, Ljava/lang/String;->charAt(I)C
    move-result v2
    const/16 v3, 0x80
    if-ge v2, v3, :non_ascii

    aget-object v2, p1, v2
    if-eqz v2, :next
    goto :write

    :non_ascii
    const/16 v3, 0x2028
    if-ne v2, v3, :check_2029
    const-string v2, "\\u2028"
    goto :write

    :check_2029
    const/16 v3, 0x2029
    if-ne v2, v3, :next
    const-string v2, "\\u2029"

    :write
    if-ge v4, v0, :write_replacement
    invoke-static {p0}, Lloops/TestDirectContinueEdgeBranch;->sink(Ljava/lang/String;)V

    :write_replacement
    invoke-static {v2}, Lloops/TestDirectContinueEdgeBranch;->sink(Ljava/lang/String;)V
    add-int/lit8 v4, v0, 0x1

    :next
    add-int/lit8 v0, v0, 0x1
    goto :loop

    :return
    return-void
.end method

.method private static sink(Ljava/lang/String;)V
    .registers 1
    return-void
.end method
