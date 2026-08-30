.class final Landroidx/room/RoomTrackingLiveData$refresh$1;
.super Lkotlin/coroutines/jvm/internal/ContinuationImpl;
.source "RoomTrackingLiveData.android.kt"

.field I$0:I
.field label:I
.field synthetic result:Ljava/lang/Object;
.field final synthetic this$0:Landroidx/room/RoomTrackingLiveData;

.method public constructor <init>(Landroidx/room/RoomTrackingLiveData;Lkotlin/coroutines/Continuation;)V
    .registers 3

    iput-object p1, p0, Landroidx/room/RoomTrackingLiveData$refresh$1;->this$0:Landroidx/room/RoomTrackingLiveData;
    invoke-direct {p0, p2}, Lkotlin/coroutines/jvm/internal/ContinuationImpl;-><init>(Lkotlin/coroutines/Continuation;)V
    return-void
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 3

    iput-object p1, p0, Landroidx/room/RoomTrackingLiveData$refresh$1;->result:Ljava/lang/Object;
    iget p1, p0, Landroidx/room/RoomTrackingLiveData$refresh$1;->label:I
    const/high16 v0, -0x80000000
    or-int/2addr p1, v0
    iput p1, p0, Landroidx/room/RoomTrackingLiveData$refresh$1;->label:I
    iget-object p1, p0, Landroidx/room/RoomTrackingLiveData$refresh$1;->this$0:Landroidx/room/RoomTrackingLiveData;
    invoke-virtual {p1, p0}, Landroidx/room/RoomTrackingLiveData;->k(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    return-object p1
.end method
