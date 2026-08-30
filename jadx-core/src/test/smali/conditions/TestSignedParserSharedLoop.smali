.class public Lconditions/TestSignedParserSharedLoop;
.super Ljava/lang/Object;

.method public static parse(Ljava/lang/String;II)I
    .registers 8

    if-lt p1, p2, :read_sign
    const/4 v0, -0x1
    return v0

    :read_sign
    invoke-virtual {p0, p1}, Ljava/lang/String;->charAt(I)C
    move-result v0
    const/16 v1, 0x2b
    if-eq v0, v1, :positive
    const/16 v1, 0x2d
    if-eq v0, v1, :negative

    const/4 v0, 0x0
    goto :parse_digits

    :negative
    const/4 v0, 0x1
    goto :consume_sign

    :positive
    const/4 v0, 0x0

    :consume_sign
    add-int/lit8 p1, p1, 0x1

    :parse_digits
    const/4 v1, 0x0

    :digit_loop
    if-ge p1, p2, :finish
    invoke-virtual {p0, p1}, Ljava/lang/String;->charAt(I)C
    move-result v2
    const/16 v3, 0x30
    if-lt v2, v3, :finish
    const/16 v3, 0x39
    if-gt v2, v3, :finish
    const/16 v3, 0x64
    if-le v1, v3, :accumulate
    const/4 v3, -0x1
    return v3

    :accumulate
    mul-int/lit8 v1, v1, 0xa
    add-int/lit8 v2, v2, -0x30
    add-int/2addr v1, v2
    add-int/lit8 p1, p1, 0x1
    goto :digit_loop

    :finish
    if-eqz v0, :return_value
    neg-int v1, v1

    :return_value
    return v1
.end method
