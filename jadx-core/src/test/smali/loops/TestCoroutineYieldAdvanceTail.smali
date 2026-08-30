.class final Lloops/TestCoroutineYieldAdvanceTail;
.super Lkotlin/coroutines/jvm/internal/RestrictedSuspendLambda;
.source "HttpHeadersMap.kt"

# interfaces
.implements Lkotlin/jvm/functions/Function2;


# annotations
.annotation system Ldalvik/annotation/EnclosingMethod;
    value = Lio/ktor/http/cio/HttpHeadersMap;->getAll(Ljava/lang/String;)Lkotlin/sequences/Sequence;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x19
    name = null
.end annotation

.annotation system Ldalvik/annotation/Signature;
    value = {
        "Lkotlin/coroutines/jvm/internal/RestrictedSuspendLambda;",
        "Lkotlin/jvm/functions/Function2<",
        "Lkotlin/sequences/SequenceScope<",
        "-",
        "Ljava/lang/CharSequence;",
        ">;",
        "Lkotlin/coroutines/Continuation<",
        "-",
        "Lkotlin/Unit;",
        ">;",
        "Ljava/lang/Object;",
        ">;"
    }
.end annotation

.annotation runtime Lkotlin/Metadata;
    d1 = {
        "\u0000\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\n\u0002\u0010\r\u0010\u0000\u001a\u00020\u0001*\u0008\u0012\u0004\u0012\u00020\u00030\u0002H\n"
    }
    d2 = {
        "<anonymous>",
        "",
        "Lkotlin/sequences/SequenceScope;",
        ""
    }
    k = 0x3
    mv = {
        0x2,
        0x2,
        0x0
    }
    xi = 0x30
.end annotation

.annotation runtime Lkotlin/coroutines/jvm/internal/DebugMetadata;
    c = "io.ktor.http.cio.HttpHeadersMap$getAll$1"
    f = "HttpHeadersMap.kt"
    i = {
        0x0,
        0x0,
        0x0
    }
    l = {
        0x5a
    }
    m = "invokeSuspend"
    n = {
        "$this$sequence",
        "hash",
        "headerIndex"
    }
    s = {
        "L$0",
        "I$0",
        "I$1"
    }
    v = 0x1
.end annotation


# instance fields
.field final synthetic $name:Ljava/lang/String;

.field I$0:I

.field I$1:I

.field private synthetic L$0:Ljava/lang/Object;

.field label:I

.field final synthetic this$0:Lio/ktor/http/cio/HttpHeadersMap;


# direct methods
.method public constructor <init>(Lio/ktor/http/cio/HttpHeadersMap;Ljava/lang/String;Lkotlin/coroutines/Continuation;)V
    .registers 4
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Lio/ktor/http/cio/HttpHeadersMap;",
            "Ljava/lang/String;",
            "Lkotlin/coroutines/Continuation<",
            "-",
            "Lloops/TestCoroutineYieldAdvanceTail;",
            ">;)V"
        }
    .end annotation

    .line 1
    iput-object p1, p0, Lloops/TestCoroutineYieldAdvanceTail;->this$0:Lio/ktor/http/cio/HttpHeadersMap;

    .line 2
    .line 3
    iput-object p2, p0, Lloops/TestCoroutineYieldAdvanceTail;->$name:Ljava/lang/String;

    .line 4
    .line 5
    const/4 p1, 0x2

    .line 6
    invoke-direct {p0, p1, p3}, Lkotlin/coroutines/jvm/internal/RestrictedSuspendLambda;-><init>(ILkotlin/coroutines/Continuation;)V

    .line 7
    .line 8
    .line 9
    return-void
.end method


# virtual methods
.method public final create(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Lkotlin/coroutines/Continuation;
    .registers 6
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Ljava/lang/Object;",
            "Lkotlin/coroutines/Continuation<",
            "*>;)",
            "Lkotlin/coroutines/Continuation<",
            "Lkotlin/Unit;",
            ">;"
        }
    .end annotation

    .line 1
    new-instance v0, Lloops/TestCoroutineYieldAdvanceTail;

    .line 2
    .line 3
    iget-object v1, p0, Lloops/TestCoroutineYieldAdvanceTail;->this$0:Lio/ktor/http/cio/HttpHeadersMap;

    .line 4
    .line 5
    iget-object v2, p0, Lloops/TestCoroutineYieldAdvanceTail;->$name:Ljava/lang/String;

    .line 6
    .line 7
    invoke-direct {v0, v1, v2, p2}, Lloops/TestCoroutineYieldAdvanceTail;-><init>(Lio/ktor/http/cio/HttpHeadersMap;Ljava/lang/String;Lkotlin/coroutines/Continuation;)V

    .line 8
    .line 9
    .line 10
    iput-object p1, v0, Lloops/TestCoroutineYieldAdvanceTail;->L$0:Ljava/lang/Object;

    .line 11
    .line 12
    return-object v0
.end method

.method public bridge synthetic invoke(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    .registers 3

    .line 1
    check-cast p1, Lkotlin/sequences/SequenceScope;

    check-cast p2, Lkotlin/coroutines/Continuation;

    invoke-virtual {p0, p1, p2}, Lloops/TestCoroutineYieldAdvanceTail;->invoke(Lkotlin/sequences/SequenceScope;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;

    move-result-object p1

    return-object p1
.end method

.method public final invoke(Lkotlin/sequences/SequenceScope;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 3
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Lkotlin/sequences/SequenceScope<",
            "-",
            "Ljava/lang/CharSequence;",
            ">;",
            "Lkotlin/coroutines/Continuation<",
            "-",
            "Lkotlin/Unit;",
            ">;)",
            "Ljava/lang/Object;"
        }
    .end annotation

    .line 2
    invoke-virtual {p0, p1, p2}, Lloops/TestCoroutineYieldAdvanceTail;->create(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Lkotlin/coroutines/Continuation;

    move-result-object p1

    check-cast p1, Lloops/TestCoroutineYieldAdvanceTail;

    sget-object p2, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;

    invoke-virtual {p1, p2}, Lloops/TestCoroutineYieldAdvanceTail;->invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object p1

    return-object p1
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 10

    .line 1
    iget-object v0, p0, Lloops/TestCoroutineYieldAdvanceTail;->L$0:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast v0, Lkotlin/sequences/SequenceScope;

    .line 4
    .line 5
    invoke-static {}, Lkotlin/coroutines/intrinsics/a;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;

    .line 6
    .line 7
    .line 8
    move-result-object v1

    .line 9
    iget v2, p0, Lloops/TestCoroutineYieldAdvanceTail;->label:I

    .line 10
    .line 11
    const/4 v3, -0x1

    .line 12
    const/4 v4, 0x1

    .line 13
    if-eqz v2, :cond_25

    .line 14
    .line 15
    if-ne v2, v4, :cond_18

    .line 16
    .line 17
    iget v2, p0, Lloops/TestCoroutineYieldAdvanceTail;->I$1:I

    .line 18
    .line 19
    iget v5, p0, Lloops/TestCoroutineYieldAdvanceTail;->I$0:I

    .line 20
    .line 21
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    .line 22
    .line 23
    .line 24
    goto :goto_76

    .line 25
    :cond_18
    new-instance p1, Ljava/lang/IllegalStateException;

    .line 26
    .line 27
    const v0, 0x624cfed3

    invoke-static {v0}, Lfixtures/obfuscation/StringDecoder;->decode(I)Ljava/lang/String;

    move-result-object v0

    .line 28
    .line 29
    invoke-direct {p1, v0}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V

    .line 30
    .line 31
    .line 32
    throw p1

    .line 33
    :cond_25
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    .line 34
    .line 35
    .line 36
    iget-object p1, p0, Lloops/TestCoroutineYieldAdvanceTail;->this$0:Lio/ktor/http/cio/HttpHeadersMap;

    .line 37
    .line 38
    invoke-virtual {p1}, Lio/ktor/http/cio/HttpHeadersMap;->getSize()I

    .line 39
    .line 40
    .line 41
    move-result p1

    .line 42
    if-nez p1, :cond_33

    .line 43
    .line 44
    sget-object p1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;

    .line 45
    .line 46
    return-object p1

    .line 47
    :cond_33
    iget-object p1, p0, Lloops/TestCoroutineYieldAdvanceTail;->$name:Ljava/lang/String;

    .line 48
    .line 49
    const/4 v2, 0x3

    .line 50
    const/4 v5, 0x0

    .line 51
    const/4 v6, 0x0

    .line 52
    invoke-static {p1, v6, v6, v2, v5}, Lio/ktor/http/cio/internals/CharsKt;->hashCodeLowerCase$default(Ljava/lang/CharSequence;IIILjava/lang/Object;)I

    .line 53
    .line 54
    .line 55
    move-result p1

    .line 56
    invoke-static {p1}, Ljava/lang/Math;->abs(I)I

    .line 57
    .line 58
    .line 59
    move-result p1

    .line 60
    iget-object v2, p0, Lloops/TestCoroutineYieldAdvanceTail;->this$0:Lio/ktor/http/cio/HttpHeadersMap;

    .line 61
    .line 62
    invoke-static {v2}, Lio/ktor/http/cio/HttpHeadersMap;->access$getHeaderCapacity$p(Lio/ktor/http/cio/HttpHeadersMap;)I

    .line 63
    .line 64
    .line 65
    move-result v2

    .line 66
    rem-int v2, p1, v2

    .line 67
    .line 68
    move v5, p1

    .line 69
    :goto_49
    iget-object p1, p0, Lloops/TestCoroutineYieldAdvanceTail;->this$0:Lio/ktor/http/cio/HttpHeadersMap;

    .line 70
    .line 71
    invoke-static {p1}, Lio/ktor/http/cio/HttpHeadersMap;->access$getHeadersData$p(Lio/ktor/http/cio/HttpHeadersMap;)Lio/ktor/http/cio/HeadersData;

    .line 72
    .line 73
    .line 74
    move-result-object p1

    .line 75
    mul-int/lit8 v6, v2, 0x6

    .line 76
    .line 77
    invoke-virtual {p1, v6}, Lio/ktor/http/cio/HeadersData;->at(I)I

    .line 78
    .line 79
    .line 80
    move-result p1

    .line 81
    if-eq p1, v3, :cond_91

    .line 82
    .line 83
    iget-object p1, p0, Lloops/TestCoroutineYieldAdvanceTail;->this$0:Lio/ktor/http/cio/HttpHeadersMap;

    .line 84
    .line 85
    iget-object v7, p0, Lloops/TestCoroutineYieldAdvanceTail;->$name:Ljava/lang/String;

    .line 86
    .line 87
    invoke-static {p1, v7, v6}, Lio/ktor/http/cio/HttpHeadersMap;->access$headerHasName(Lio/ktor/http/cio/HttpHeadersMap;Ljava/lang/CharSequence;I)Z

    .line 88
    .line 89
    .line 90
    move-result p1

    .line 91
    if-eqz p1, :cond_87

    .line 92
    .line 93
    iget-object p1, p0, Lloops/TestCoroutineYieldAdvanceTail;->this$0:Lio/ktor/http/cio/HttpHeadersMap;

    .line 94
    .line 95
    invoke-virtual {p1, v6}, Lio/ktor/http/cio/HttpHeadersMap;->valueAtOffset(I)Ljava/lang/CharSequence;

    .line 96
    .line 97
    .line 98
    move-result-object p1

    .line 99
    iput-object v0, p0, Lloops/TestCoroutineYieldAdvanceTail;->L$0:Ljava/lang/Object;

    .line 100
    .line 101
    iput v5, p0, Lloops/TestCoroutineYieldAdvanceTail;->I$0:I

    .line 102
    .line 103
    iput v2, p0, Lloops/TestCoroutineYieldAdvanceTail;->I$1:I

    .line 104
    .line 105
    iput v4, p0, Lloops/TestCoroutineYieldAdvanceTail;->label:I

    .line 106
    .line 107
    invoke-virtual {v0, p1, p0}, Lkotlin/sequences/SequenceScope;->yield(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;

    .line 108
    .line 109
    .line 110
    move-result-object p1

    .line 111
    if-ne p1, v1, :cond_76

    .line 112
    .line 113
    return-object v1

    .line 114
    :cond_76
    :goto_76
    iget-object p1, p0, Lloops/TestCoroutineYieldAdvanceTail;->this$0:Lio/ktor/http/cio/HttpHeadersMap;

    .line 115
    .line 116
    invoke-static {p1}, Lio/ktor/http/cio/HttpHeadersMap;->access$getHeadersData$p(Lio/ktor/http/cio/HttpHeadersMap;)Lio/ktor/http/cio/HeadersData;

    .line 117
    .line 118
    .line 119
    move-result-object p1

    .line 120
    mul-int/lit8 v2, v2, 0x6

    .line 121
    .line 122
    add-int/lit8 v2, v2, 0x5

    .line 123
    .line 124
    invoke-virtual {p1, v2}, Lio/ktor/http/cio/HeadersData;->at(I)I

    .line 125
    .line 126
    .line 127
    move-result v2

    .line 128
    if-eq v2, v3, :cond_91

    .line 129
    .line 130
    goto :goto_49

    .line 131
    :cond_87
    add-int/lit8 v2, v2, 0x1

    .line 132
    .line 133
    iget-object p1, p0, Lloops/TestCoroutineYieldAdvanceTail;->this$0:Lio/ktor/http/cio/HttpHeadersMap;

    .line 134
    .line 135
    invoke-static {p1}, Lio/ktor/http/cio/HttpHeadersMap;->access$getHeaderCapacity$p(Lio/ktor/http/cio/HttpHeadersMap;)I

    .line 136
    .line 137
    .line 138
    move-result p1

    .line 139
    rem-int/2addr v2, p1

    .line 140
    goto :goto_49

    .line 141
    :cond_91
    sget-object p1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;

    .line 142
    .line 143
    return-object p1
.end method
