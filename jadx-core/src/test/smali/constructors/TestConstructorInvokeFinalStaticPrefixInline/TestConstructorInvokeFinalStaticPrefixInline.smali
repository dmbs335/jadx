.class public Lconstructors/TestConstructorInvokeFinalStaticPrefixInline;
.super Lconstructors/TestConstructorInvokeFinalStaticPrefixInlineParent;

.method public constructor <init>()V
    .locals 3
    invoke-static {}, Lconstructors/TestConstructorInvokeFinalStaticPrefixInline;->factory()Lconstructors/TestConstructorInvokeFinalStaticPrefixInlineChain;
    move-result-object v0
    sget-object v1, Ljava/nio/charset/CodingErrorAction;->REPLACE:Ljava/nio/charset/CodingErrorAction;
    invoke-virtual {v0, v1}, Lconstructors/TestConstructorInvokeFinalStaticPrefixInlineChain;->first(Ljava/nio/charset/CodingErrorAction;)Lconstructors/TestConstructorInvokeFinalStaticPrefixInlineChain;
    move-result-object v2
    invoke-virtual {v2, v1}, Lconstructors/TestConstructorInvokeFinalStaticPrefixInlineChain;->second(Ljava/nio/charset/CodingErrorAction;)Lconstructors/TestConstructorInvokeFinalStaticPrefixInlineChain;
    move-result-object v2
    invoke-direct {p0, v2}, Lconstructors/TestConstructorInvokeFinalStaticPrefixInlineParent;-><init>(Lconstructors/TestConstructorInvokeFinalStaticPrefixInlineChain;)V
    return-void
.end method

.method private static factory()Lconstructors/TestConstructorInvokeFinalStaticPrefixInlineChain;
    .locals 1
    new-instance v0, Lconstructors/TestConstructorInvokeFinalStaticPrefixInlineChain;
    invoke-direct {v0}, Lconstructors/TestConstructorInvokeFinalStaticPrefixInlineChain;-><init>()V
    return-object v0
.end method
