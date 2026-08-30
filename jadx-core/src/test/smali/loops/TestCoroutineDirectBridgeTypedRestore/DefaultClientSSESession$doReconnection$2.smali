###### Class io.ktor.client.plugins.sse.DefaultClientSSESession$doReconnection$2 (io.ktor.client.plugins.sse.DefaultClientSSESession$doReconnection$2)
.class final Lio/ktor/client/plugins/sse/DefaultClientSSESession$doReconnection$2;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;
.source "DefaultClientSSESession.kt"

# interfaces
.implements Lkotlin/jvm/functions/Function2;


# annotations
.annotation system Ldalvik/annotation/EnclosingMethod;
    value = Lio/ktor/client/plugins/sse/DefaultClientSSESession;->f(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x19
    name = null
.end annotation

.annotation system Ldalvik/annotation/Signature;
    value = {
        "Lkotlin/coroutines/jvm/internal/SuspendLambda;",
        "Lkotlin/jvm/functions/Function2<",
        "Lkotlinx/coroutines/CoroutineScope;",
        "Lkotlin/coroutines/Continuation<",
        "-",
        "Lkotlin/Unit;",
        ">;",
        "Ljava/lang/Object;",
        ">;"
    }
.end annotation

.annotation system Ldalvik/annotation/SourceDebugExtension;
    value = "SMAP\nDefaultClientSSESession.kt\nKotlin\n*S Kotlin\n*F\n+ 1 DefaultClientSSESession.kt\nio/ktor/client/plugins/sse/DefaultClientSSESession$doReconnection$2\n+ 2 Logger.kt\nio/ktor/util/logging/LoggerKt\n*L\n1#1,253:1\n38#2,2:254\n38#2,2:256\n38#2,2:258\n38#2,2:260\n*S KotlinDebug\n*F\n+ 1 DefaultClientSSESession.kt\nio/ktor/client/plugins/sse/DefaultClientSSESession$doReconnection$2\n*L\n106#1:254,2\n111#1:256,2\n122#1:258,2\n127#1:260,2\n*E\n"
.end annotation

.annotation runtime Lkotlin/Metadata;
    d1 = {
        "\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"
    }
    d2 = {
        "<anonymous>",
        "",
        "Lkotlinx/coroutines/CoroutineScope;"
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
    c = "io.ktor.client.plugins.sse.DefaultClientSSESession$doReconnection$2"
    f = "DefaultClientSSESession.kt"
    i = {
        0x0,
        0x1,
        0x1,
        0x2,
        0x2,
        0x2
    }
    l = {
        0x67,
        0x6e,
        0x70
    }
    m = "invokeSuspend"
    n = {
        "retries",
        "retries",
        "reconnectionRequest",
        "retries",
        "reconnectionRequest",
        "reconnectionResponse"
    }
    s = {
        "L$0",
        "L$0",
        "L$1",
        "L$0",
        "L$1",
        "L$2"
    }
    v = 0x1
.end annotation

.annotation build Lkotlin/jvm/internal/SourceDebugExtension;
    value = {
        "SMAP\nDefaultClientSSESession.kt\nKotlin\n*S Kotlin\n*F\n+ 1 DefaultClientSSESession.kt\nio/ktor/client/plugins/sse/DefaultClientSSESession$doReconnection$2\n+ 2 Logger.kt\nio/ktor/util/logging/LoggerKt\n*L\n1#1,253:1\n38#2,2:254\n38#2,2:256\n38#2,2:258\n38#2,2:260\n*S KotlinDebug\n*F\n+ 1 DefaultClientSSESession.kt\nio/ktor/client/plugins/sse/DefaultClientSSESession$doReconnection$2\n*L\n106#1:254,2\n111#1:256,2\n122#1:258,2\n127#1:260,2\n*E\n"
    }
.end annotation


# instance fields
.field L$0:Ljava/lang/Object;

.field L$1:Ljava/lang/Object;

.field L$2:Ljava/lang/Object;

.field label:I

.field final synthetic this$0:Lio/ktor/client/plugins/sse/DefaultClientSSESession;


# direct methods
.method public constructor <init>(Lio/ktor/client/plugins/sse/DefaultClientSSESession;Lkotlin/coroutines/Continuation;)V
    .registers 3
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Lio/ktor/client/plugins/sse/DefaultClientSSESession;",
            "Lkotlin/coroutines/Continuation<",
            "-",
            "Lio/ktor/client/plugins/sse/DefaultClientSSESession$doReconnection$2;",
            ">;)V"
        }
    .end annotation

    .line 1
    iput-object p1, p0, Lio/ktor/client/plugins/sse/DefaultClientSSESession$doReconnection$2;->this$0:Lio/ktor/client/plugins/sse/DefaultClientSSESession;

    .line 2
    .line 3
    const/4 p1, 0x2

    .line 4
    invoke-direct {p0, p1, p2}, Lkotlin/coroutines/jvm/internal/SuspendLambda;-><init>(ILkotlin/coroutines/Continuation;)V

    .line 5
    .line 6
    .line 7
    return-void
.end method


# virtual methods
.method public final create(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Lkotlin/coroutines/Continuation;
    .registers 4
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
    new-instance p1, Lio/ktor/client/plugins/sse/DefaultClientSSESession$doReconnection$2;

    .line 2
    .line 3
    iget-object v0, p0, Lio/ktor/client/plugins/sse/DefaultClientSSESession$doReconnection$2;->this$0:Lio/ktor/client/plugins/sse/DefaultClientSSESession;

    .line 4
    .line 5
    invoke-direct {p1, v0, p2}, Lio/ktor/client/plugins/sse/DefaultClientSSESession$doReconnection$2;-><init>(Lio/ktor/client/plugins/sse/DefaultClientSSESession;Lkotlin/coroutines/Continuation;)V

    .line 6
    .line 7
    .line 8
    return-object p1
.end method

.method public bridge synthetic invoke(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    .registers 3

    .line 1
    check-cast p1, Lkotlinx/coroutines/CoroutineScope;

    check-cast p2, Lkotlin/coroutines/Continuation;

    invoke-virtual {p0, p1, p2}, Lio/ktor/client/plugins/sse/DefaultClientSSESession$doReconnection$2;->invoke(Lkotlinx/coroutines/CoroutineScope;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;

    move-result-object p1

    return-object p1
.end method

.method public final invoke(Lkotlinx/coroutines/CoroutineScope;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 3
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Lkotlinx/coroutines/CoroutineScope;",
            "Lkotlin/coroutines/Continuation<",
            "-",
            "Lkotlin/Unit;",
            ">;)",
            "Ljava/lang/Object;"
        }
    .end annotation

    .line 2
    invoke-virtual {p0, p1, p2}, Lio/ktor/client/plugins/sse/DefaultClientSSESession$doReconnection$2;->create(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Lkotlin/coroutines/Continuation;

    move-result-object p1

    check-cast p1, Lio/ktor/client/plugins/sse/DefaultClientSSESession$doReconnection$2;

    sget-object p2, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;

    invoke-virtual {p1, p2}, Lio/ktor/client/plugins/sse/DefaultClientSSESession$doReconnection$2;->invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object p1

    return-object p1
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 11

    .line 1
    invoke-static {}, Lkotlin/coroutines/intrinsics/a;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;

    .line 2
    .line 3
    .line 4
    move-result-object v0

    .line 5
    iget v1, p0, Lio/ktor/client/plugins/sse/DefaultClientSSESession$doReconnection$2;->label:I

    .line 6
    .line 7
    const/4 v2, 0x3

    .line 8
    const/4 v3, 0x2

    .line 9
    const/4 v4, 0x1

    .line 10
    if-eqz v1, :cond_48

    .line 11
    .line 12
    if-eq v1, v4, :cond_3f

    .line 13
    .line 14
    if-eq v1, v3, :cond_32

    .line 15
    .line 16
    if-ne v1, v2, :cond_25

    .line 17
    .line 18
    move-object v8, p1

    iget-object v1, p0, Lio/ktor/client/plugins/sse/DefaultClientSSESession$doReconnection$2;->L$2:Ljava/lang/Object;

    .line 19
    .line 20
    check-cast v1, Lio/ktor/client/statement/HttpResponse;

    .line 21
    .line 22
    iget-object v5, p0, Lio/ktor/client/plugins/sse/DefaultClientSSESession$doReconnection$2;->L$1:Ljava/lang/Object;

    .line 23
    .line 24
    check-cast v5, Lio/ktor/client/request/HttpRequestBuilder;

    .line 25
    .line 26
    iget-object v5, p0, Lio/ktor/client/plugins/sse/DefaultClientSSESession$doReconnection$2;->L$0:Ljava/lang/Object;

    .line 27
    .line 28
    check-cast v5, Lkotlin/jvm/internal/Ref$IntRef;

    .line 29
    .line 30
    :try_start_1d
    invoke-static {v8}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    const/4 v6, 0x0
    :try_end_20
    .catchall {:try_start_1d .. :try_end_20} :catchall_22

    .line 31
    .line 32
    .line 33
    goto/16 :goto_113

    .line 34
    .line 35
    :catchall_22
    move-exception p1

    .line 36
    goto/16 :goto_135

    .line 37
    .line 38
    :cond_25
    new-instance p1, Ljava/lang/IllegalStateException;

    .line 39
    .line 40
    const v0, 0x624cfed3

    invoke-static {v0}, Lfixtures/obfuscation/StringDecoder;->˔˓̏ʍ(I)Ljava/lang/String;

    move-result-object v0

    .line 41
    .line 42
    invoke-direct {p1, v0}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V

    .line 43
    .line 44
    .line 45
    throw p1

    .line 46
    :cond_32
    iget-object v1, p0, Lio/ktor/client/plugins/sse/DefaultClientSSESession$doReconnection$2;->L$1:Ljava/lang/Object;

    .line 47
    .line 48
    check-cast v1, Lio/ktor/client/request/HttpRequestBuilder;

    .line 49
    .line 50
    iget-object v5, p0, Lio/ktor/client/plugins/sse/DefaultClientSSESession$doReconnection$2;->L$0:Ljava/lang/Object;

    .line 51
    .line 52
    check-cast v5, Lkotlin/jvm/internal/Ref$IntRef;

    .line 53
    .line 54
    :try_start_3a
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_3d
    .catchall {:try_start_3a .. :try_end_3d} :catchall_22

    .line 55
    .line 56
    .line 57
    goto/16 :goto_d7

    .line 58
    .line 59
    :cond_3f
    iget-object v1, p0, Lio/ktor/client/plugins/sse/DefaultClientSSESession$doReconnection$2;->L$0:Ljava/lang/Object;

    .line 60
    .line 61
    move-object v5, v1

    .line 62
    check-cast v5, Lkotlin/jvm/internal/Ref$IntRef;

    .line 63
    .line 64
    :try_start_44
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_47
    .catchall {:try_start_44 .. :try_end_47} :catchall_22

    .line 65
    .line 66
    .line 67
    goto :goto_7d

    .line 68
    :cond_48
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    .line 69
    .line 70
    .line 71
    new-instance p1, Lkotlin/jvm/internal/Ref$IntRef;

    .line 72
    .line 73
    invoke-direct {p1}, Lkotlin/jvm/internal/Ref$IntRef;-><init>()V

    .line 74
    .line 75
    .line 76
    iput v4, p1, Lkotlin/jvm/internal/Ref$IntRef;->element:I

    .line 77
    .line 78
    move-object v5, p1

    .line 79
    :goto_53
    iget p1, v5, Lkotlin/jvm/internal/Ref$IntRef;->element:I

    .line 80
    .line 81
    iget-object v1, p0, Lio/ktor/client/plugins/sse/DefaultClientSSESession$doReconnection$2;->this$0:Lio/ktor/client/plugins/sse/DefaultClientSSESession;

    .line 82
    .line 83
    invoke-static {v1}, Lio/ktor/client/plugins/sse/DefaultClientSSESession;->access$getMaxReconnectionAttempts$p(Lio/ktor/client/plugins/sse/DefaultClientSSESession;)I

    .line 84
    .line 85
    .line 86
    move-result v1

    .line 87
    if-gt p1, v1, :cond_1a4

    .line 88
    .line 89
    :try_start_5d
    iget-object p1, p0, Lio/ktor/client/plugins/sse/DefaultClientSSESession$doReconnection$2;->this$0:Lio/ktor/client/plugins/sse/DefaultClientSSESession;

    .line 90
    .line 91
    invoke-static {p1}, Lio/ktor/client/plugins/sse/DefaultClientSSESession;->access$getInput$p(Lio/ktor/client/plugins/sse/DefaultClientSSESession;)Lio/ktor/utils/io/ByteReadChannel;

    .line 92
    .line 93
    .line 94
    move-result-object p1

    .line 95
    invoke-static {p1}, Lio/ktor/utils/io/ByteReadChannelKt;->cancel(Lio/ktor/utils/io/ByteReadChannel;)V

    .line 96
    .line 97
    .line 98
    iget-object p1, p0, Lio/ktor/client/plugins/sse/DefaultClientSSESession$doReconnection$2;->this$0:Lio/ktor/client/plugins/sse/DefaultClientSSESession;

    .line 99
    .line 100
    invoke-static {p1}, Lio/ktor/client/plugins/sse/DefaultClientSSESession;->access$getReconnectionTimeMillis$p(Lio/ktor/client/plugins/sse/DefaultClientSSESession;)J

    .line 101
    .line 102
    .line 103
    move-result-wide v6

    .line 104
    iput-object v5, p0, Lio/ktor/client/plugins/sse/DefaultClientSSESession$doReconnection$2;->L$0:Ljava/lang/Object;

    .line 105
    .line 106
    const/4 p1, 0x0

    .line 107
    iput-object p1, p0, Lio/ktor/client/plugins/sse/DefaultClientSSESession$doReconnection$2;->L$1:Ljava/lang/Object;

    .line 108
    .line 109
    iput-object p1, p0, Lio/ktor/client/plugins/sse/DefaultClientSSESession$doReconnection$2;->L$2:Ljava/lang/Object;

    .line 110
    .line 111
    iput v4, p0, Lio/ktor/client/plugins/sse/DefaultClientSSESession$doReconnection$2;->label:I

    .line 112
    .line 113
    invoke-static {v6, v7, p0}, Lkotlinx/coroutines/DelayKt;->delay(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;

    .line 114
    .line 115
    .line 116
    move-result-object p1

    .line 117
    if-ne p1, v0, :cond_7d

    .line 118
    .line 119
    goto/16 :goto_111

    .line 120
    .line 121
    :cond_7d
    :goto_7d
    iget-object p1, p0, Lio/ktor/client/plugins/sse/DefaultClientSSESession$doReconnection$2;->this$0:Lio/ktor/client/plugins/sse/DefaultClientSSESession;

    .line 122
    .line 123
    invoke-static {p1}, Lio/ktor/client/plugins/sse/DefaultClientSSESession;->access$getRequestForReconnection(Lio/ktor/client/plugins/sse/DefaultClientSSESession;)Lio/ktor/client/request/HttpRequestBuilder;

    .line 124
    .line 125
    .line 126
    move-result-object v1

    .line 127
    invoke-static {}, Lio/ktor/client/plugins/sse/SSEKt;->getLOGGER()Lorg/slf4j/Logger;

    .line 128
    .line 129
    .line 130
    move-result-object p1

    .line 131
    iget-object v6, p0, Lio/ktor/client/plugins/sse/DefaultClientSSESession$doReconnection$2;->this$0:Lio/ktor/client/plugins/sse/DefaultClientSSESession;

    .line 132
    .line 133
    invoke-static {p1}, Lio/ktor/util/logging/LoggerJvmKt;->isTraceEnabled(Lorg/slf4j/Logger;)Z

    .line 134
    .line 135
    .line 136
    move-result v7

    .line 137
    if-eqz v7, :cond_c4

    .line 138
    .line 139
    new-instance v7, Ljava/lang/StringBuilder;

    .line 140
    .line 141
    invoke-direct {v7}, Ljava/lang/StringBuilder;-><init>()V

    .line 142
    .line 143
    .line 144
    const-string v8, "Sending SSE request "

    .line 145
    .line 146
    invoke-virtual {v7, v8}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 147
    .line 148
    .line 149
    invoke-virtual {v1}, Lio/ktor/client/request/HttpRequestBuilder;->getUrl()Lio/ktor/http/URLBuilder;

    .line 150
    .line 151
    .line 152
    move-result-object v8

    .line 153
    invoke-virtual {v7, v8}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    .line 154
    .line 155
    .line 156
    const-string v8, " (attempt "

    .line 157
    .line 158
    invoke-virtual {v7, v8}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 159
    .line 160
    .line 161
    iget v8, v5, Lkotlin/jvm/internal/Ref$IntRef;->element:I

    .line 162
    .line 163
    add-int/2addr v8, v4

    .line 164
    invoke-virtual {v7, v8}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    .line 165
    .line 166
    .line 167
    const/16 v8, 0x2f

    .line 168
    .line 169
    invoke-virtual {v7, v8}, Ljava/lang/StringBuilder;->append(C)Ljava/lang/StringBuilder;

    .line 170
    .line 171
    .line 172
    invoke-static {v6}, Lio/ktor/client/plugins/sse/DefaultClientSSESession;->access$getMaxReconnectionAttempts$p(Lio/ktor/client/plugins/sse/DefaultClientSSESession;)I

    .line 173
    .line 174
    .line 175
    move-result v6

    .line 176
    add-int/2addr v6, v4

    .line 177
    invoke-virtual {v7, v6}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    .line 178
    .line 179
    .line 180
    const/16 v6, 0x29

    .line 181
    .line 182
    invoke-virtual {v7, v6}, Ljava/lang/StringBuilder;->append(C)Ljava/lang/StringBuilder;

    .line 183
    .line 184
    .line 185
    invoke-virtual {v7}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    .line 186
    .line 187
    .line 188
    move-result-object v6

    .line 189
    invoke-interface {p1, v6}, Lorg/slf4j/Logger;->trace(Ljava/lang/String;)V

    .line 190
    .line 191
    .line 192
    :cond_c4
    iget-object p1, p0, Lio/ktor/client/plugins/sse/DefaultClientSSESession$doReconnection$2;->this$0:Lio/ktor/client/plugins/sse/DefaultClientSSESession;

    .line 193
    .line 194
    invoke-static {p1}, Lio/ktor/client/plugins/sse/DefaultClientSSESession;->access$getClientForReconnection$p(Lio/ktor/client/plugins/sse/DefaultClientSSESession;)Lio/ktor/client/HttpClient;

    .line 195
    .line 196
    .line 197
    move-result-object p1

    .line 198
    iput-object v5, p0, Lio/ktor/client/plugins/sse/DefaultClientSSESession$doReconnection$2;->L$0:Ljava/lang/Object;

    .line 199
    .line 200
    iput-object v1, p0, Lio/ktor/client/plugins/sse/DefaultClientSSESession$doReconnection$2;->L$1:Ljava/lang/Object;

    .line 201
    .line 202
    iput v3, p0, Lio/ktor/client/plugins/sse/DefaultClientSSESession$doReconnection$2;->label:I

    .line 203
    .line 204
    invoke-virtual {p1, v1, p0}, Lio/ktor/client/HttpClient;->execute$ktor_client_core(Lio/ktor/client/request/HttpRequestBuilder;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;

    .line 205
    .line 206
    .line 207
    move-result-object p1

    .line 208
    if-ne p1, v0, :cond_d7

    .line 209
    .line 210
    goto :goto_111

    .line 211
    :cond_d7
    :goto_d7
    check-cast p1, Lio/ktor/client/call/HttpClientCall;

    .line 212
    .line 213
    invoke-virtual {p1}, Lio/ktor/client/call/HttpClientCall;->getResponse()Lio/ktor/client/statement/HttpResponse;

    .line 214
    .line 215
    .line 216
    move-result-object p1

    .line 217
    invoke-static {}, Lio/ktor/client/plugins/sse/SSEKt;->getLOGGER()Lorg/slf4j/Logger;

    .line 218
    .line 219
    .line 220
    move-result-object v6

    .line 221
    invoke-static {v6}, Lio/ktor/util/logging/LoggerJvmKt;->isTraceEnabled(Lorg/slf4j/Logger;)Z

    .line 222
    .line 223
    .line 224
    move-result v7

    .line 225
    if-eqz v7, :cond_ff

    .line 226
    .line 227
    new-instance v7, Ljava/lang/StringBuilder;

    .line 228
    .line 229
    invoke-direct {v7}, Ljava/lang/StringBuilder;-><init>()V

    .line 230
    .line 231
    .line 232
    const-string v8, "Receive response for reconnection SSE request to "

    .line 233
    .line 234
    invoke-virtual {v7, v8}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 235
    .line 236
    .line 237
    invoke-virtual {v1}, Lio/ktor/client/request/HttpRequestBuilder;->getUrl()Lio/ktor/http/URLBuilder;

    .line 238
    .line 239
    .line 240
    move-result-object v8

    .line 241
    invoke-virtual {v7, v8}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    .line 242
    .line 243
    .line 244
    invoke-virtual {v7}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    .line 245
    .line 246
    .line 247
    move-result-object v7

    .line 248
    invoke-interface {v6, v7}, Lorg/slf4j/Logger;->trace(Ljava/lang/String;)V

    .line 249
    .line 250
    .line 251
    :cond_ff
    iput-object v5, p0, Lio/ktor/client/plugins/sse/DefaultClientSSESession$doReconnection$2;->L$0:Ljava/lang/Object;

    .line 252
    .line 253
    invoke-static {v1}, Lkotlin/coroutines/jvm/internal/SpillingKt;->nullOutSpilledVariable(Ljava/lang/Object;)Ljava/lang/Object;

    .line 254
    .line 255
    .line 256
    move-result-object v1

    .line 257
    iput-object v1, p0, Lio/ktor/client/plugins/sse/DefaultClientSSESession$doReconnection$2;->L$1:Ljava/lang/Object;

    .line 258
    .line 259
    iput-object p1, p0, Lio/ktor/client/plugins/sse/DefaultClientSSESession$doReconnection$2;->L$2:Ljava/lang/Object;

    .line 260
    .line 261
    iput v2, p0, Lio/ktor/client/plugins/sse/DefaultClientSSESession$doReconnection$2;->label:I

    .line 262
    .line 263
    invoke-static {p1, p0}, Lio/ktor/client/plugins/sse/SSEKt;->checkResponse(Lio/ktor/client/statement/HttpResponse;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;

    .line 264
    .line 265
    .line 266
    move-result-object v1

    .line 267
    if-ne v1, v0, :cond_112

    .line 268
    .line 269
    :goto_111
    return-object v0

    .line 270
    :cond_112
    move-object v1, p1

    const/4 v6, 0x0

    .line 271
    :goto_113
    invoke-virtual {v1}, Lio/ktor/client/statement/HttpResponse;->getStatus()Lio/ktor/http/HttpStatusCode;

    .line 272
    .line 273
    .line 274
    move-result-object p1

    .line 275
    sget-object v6, Lio/ktor/http/HttpStatusCode;->Companion:Lio/ktor/http/HttpStatusCode$Companion;

    .line 276
    .line 277
    invoke-virtual {v6}, Lio/ktor/http/HttpStatusCode$Companion;->getNoContent()Lio/ktor/http/HttpStatusCode;

    .line 278
    .line 279
    .line 280
    move-result-object v6

    .line 281
    invoke-static {p1, v6}, Lkotlin/jvm/internal/Intrinsics;->areEqual(Ljava/lang/Object;Ljava/lang/Object;)Z

    .line 282
    .line 283
    .line 284
    move-result p1

    .line 285
    if-eqz p1, :cond_129

    .line 286
    .line 287
    iget-object p1, p0, Lio/ktor/client/plugins/sse/DefaultClientSSESession$doReconnection$2;->this$0:Lio/ktor/client/plugins/sse/DefaultClientSSESession;

    .line 288
    .line 289
    const/4 v6, 0x0

    .line 290
    invoke-static {p1, v6}, Lio/ktor/client/plugins/sse/DefaultClientSSESession;->access$setNeedToReconnect$p(Lio/ktor/client/plugins/sse/DefaultClientSSESession;Z)V

    .line 291
    .line 292
    .line 293
    :cond_129
    iget-object p1, p0, Lio/ktor/client/plugins/sse/DefaultClientSSESession$doReconnection$2;->this$0:Lio/ktor/client/plugins/sse/DefaultClientSSESession;

    .line 294
    .line 295
    invoke-virtual {v1}, Lio/ktor/client/statement/HttpResponse;->getRawContent()Lio/ktor/utils/io/ByteReadChannel;

    .line 296
    .line 297
    .line 298
    move-result-object v1

    .line 299
    invoke-static {p1, v1}, Lio/ktor/client/plugins/sse/DefaultClientSSESession;->access$setInput$p(Lio/ktor/client/plugins/sse/DefaultClientSSESession;Lio/ktor/utils/io/ByteReadChannel;)V

    .line 300
    .line 301
    .line 302
    sget-object p1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    :try_end_134
    .catchall {:try_start_5d .. :try_end_134} :catchall_22

    .line 303
    .line 304
    return-object p1

    .line 305
    :goto_135
    iget v1, v5, Lkotlin/jvm/internal/Ref$IntRef;->element:I

    .line 306
    .line 307
    iget-object v6, p0, Lio/ktor/client/plugins/sse/DefaultClientSSESession$doReconnection$2;->this$0:Lio/ktor/client/plugins/sse/DefaultClientSSESession;

    .line 308
    .line 309
    invoke-static {v6}, Lio/ktor/client/plugins/sse/DefaultClientSSESession;->access$getMaxReconnectionAttempts$p(Lio/ktor/client/plugins/sse/DefaultClientSSESession;)I

    .line 310
    .line 311
    .line 312
    move-result v6

    .line 313
    if-ne v1, v6, :cond_170

    .line 314
    .line 315
    invoke-static {}, Lio/ktor/client/plugins/sse/SSEKt;->getLOGGER()Lorg/slf4j/Logger;

    .line 316
    .line 317
    .line 318
    move-result-object v0

    .line 319
    iget-object v1, p0, Lio/ktor/client/plugins/sse/DefaultClientSSESession$doReconnection$2;->this$0:Lio/ktor/client/plugins/sse/DefaultClientSSESession;

    .line 320
    .line 321
    invoke-static {v0}, Lio/ktor/util/logging/LoggerJvmKt;->isTraceEnabled(Lorg/slf4j/Logger;)Z

    .line 322
    .line 323
    .line 324
    move-result v2

    .line 325
    if-eqz v2, :cond_16f

    .line 326
    .line 327
    new-instance v2, Ljava/lang/StringBuilder;

    .line 328
    .line 329
    const v3, 0x2f717de8

    invoke-static {v3}, Lfixtures/obfuscation/StringDecoder;->͍ɏɌƒ(I)Ljava/lang/String;

    move-result-object v3

    .line 330
    .line 331
    invoke-direct {v2, v3}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    .line 332
    .line 333
    .line 334
    invoke-static {v1}, Lio/ktor/client/plugins/sse/DefaultClientSSESession;->access$getMaxReconnectionAttempts$p(Lio/ktor/client/plugins/sse/DefaultClientSSESession;)I

    .line 335
    .line 336
    .line 337
    move-result v1

    .line 338
    invoke-virtual {v2, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    .line 339
    .line 340
    .line 341
    const v1, -0x132b45de

    invoke-static {v1}, Lfixtures/obfuscation/StringDecoder;->͎ƒˌɏ(I)Ljava/lang/String;

    move-result-object v1

    .line 342
    .line 343
    invoke-virtual {v2, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 344
    .line 345
    .line 346
    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    .line 347
    .line 348
    .line 349
    move-result-object v1

    .line 350
    invoke-interface {v0, v1}, Lorg/slf4j/Logger;->trace(Ljava/lang/String;)V

    .line 351
    .line 352
    .line 353
    :cond_16f
    throw p1

    .line 354
    :cond_170
    invoke-static {}, Lio/ktor/client/plugins/sse/SSEKt;->getLOGGER()Lorg/slf4j/Logger;

    .line 355
    .line 356
    .line 357
    move-result-object p1

    .line 358
    invoke-static {p1}, Lio/ktor/util/logging/LoggerJvmKt;->isTraceEnabled(Lorg/slf4j/Logger;)Z

    .line 359
    .line 360
    .line 361
    move-result v1

    .line 362
    if-eqz v1, :cond_19d

    .line 363
    .line 364
    new-instance v1, Ljava/lang/StringBuilder;

    .line 365
    .line 366
    const v6, 0x2f717fe8

    invoke-static {v6}, Lfixtures/obfuscation/StringDecoder;->͍ɏɌƒ(I)Ljava/lang/String;

    move-result-object v6

    .line 367
    .line 368
    invoke-direct {v1, v6}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    .line 369
    .line 370
    .line 371
    iget v6, v5, Lkotlin/jvm/internal/Ref$IntRef;->element:I

    .line 372
    .line 373
    add-int/2addr v6, v4

    .line 374
    invoke-virtual {v1, v6}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    .line 375
    .line 376
    .line 377
    const v6, 0x1f97c247

    invoke-static {v6}, Lfixtures/obfuscation/StringDecoder;->ƌ̒̌Ƒ(I)Ljava/lang/String;

    move-result-object v6

    .line 378
    .line 379
    invoke-virtual {v1, v6}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 380
    .line 381
    .line 382
    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    .line 383
    .line 384
    .line 385
    move-result-object v1

    .line 386
    invoke-interface {p1, v1}, Lorg/slf4j/Logger;->trace(Ljava/lang/String;)V

    .line 387
    .line 388
    .line 389
    :cond_19d
    iget p1, v5, Lkotlin/jvm/internal/Ref$IntRef;->element:I

    .line 390
    .line 391
    add-int/2addr p1, v4

    .line 392
    iput p1, v5, Lkotlin/jvm/internal/Ref$IntRef;->element:I

    .line 393
    .line 394
    goto/16 :goto_53

    .line 395
    .line 396
    :cond_1a4
    sget-object p1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;

    .line 397
    .line 398
    return-object p1
.end method
