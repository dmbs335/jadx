.class public Lloops/TestOverwrittenReceiverLoop;
.super Ljava/lang/Object;

.field public static replacement:Lloops/TestOverwrittenReceiverLoop;
.field public static textReplacement:Ljava/lang/String;
.field public instanceReplacement:Lloops/TestOverwrittenReceiverLoop;

.method public constructor <init>()V
    .registers 1

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method

.method public static getReplacement()Lloops/TestOverwrittenReceiverLoop;
    .registers 1

    sget-object v0, Lloops/TestOverwrittenReceiverLoop;->replacement:Lloops/TestOverwrittenReceiverLoop;
    return-object v0
.end method

.method public static getOtherReplacement()Ljava/lang/String;
    .registers 1

    sget-object v0, Lloops/TestOverwrittenReceiverLoop;->textReplacement:Ljava/lang/String;
    return-object v0
.end method

.method public static native mayThrow()V
.end method

.method public static native recordCatch()V
.end method

.method public final check(I)I
    .registers 3

    :loop
    if-lez p1, :done
    const/4 p0, 0x0
    add-int/lit8 p1, p1, -0x1
    goto :loop

    :done
    if-eqz p0, :null_receiver
    const/4 v0, 0x1
    return v0

    :null_receiver
    const/4 v0, 0x2
    return v0
.end method

.method public final checkAfterBranch(Z)I
    .registers 3

    if-eqz p1, :done
    const/4 p0, 0x0

    :done
    if-eqz p0, :null_receiver
    const/4 v0, 0x1
    return v0

    :null_receiver
    const/4 v0, 0x2
    return v0
.end method

.method public final checkAfterBranchReplacement(Lloops/TestOverwrittenReceiverLoop;Z)I
    .registers 4

    if-eqz p2, :done
    move-object p0, p1

    :done
    if-eqz p0, :null_receiver
    const/4 v0, 0x1
    return v0

    :null_receiver
    const/4 v0, 0x2
    return v0
.end method

.method public final checkAfterFreshReceiver(Z)I
    .registers 3

    if-eqz p1, :done
    new-instance p0, Lloops/TestOverwrittenReceiverLoop;
    invoke-direct {p0}, Lloops/TestOverwrittenReceiverLoop;-><init>()V

    :done
    invoke-virtual {p0}, Ljava/lang/Object;->hashCode()I
    move-result v0
    return v0
.end method

.method public final checkAfterFreshArray(Z)I
    .registers 4

    if-eqz p1, :done
    const/4 v0, 0x1
    new-array p0, v0, [I

    :done
    invoke-virtual {p0}, Ljava/lang/Object;->hashCode()I
    move-result v0
    return v0
.end method

.method public final checkAfterStringReplacement(Z)I
    .registers 3

    if-eqz p1, :done
    const-string p0, "replacement"

    :done
    invoke-virtual {p0}, Ljava/lang/Object;->hashCode()I
    move-result v0
    return v0
.end method

.method public final checkAfterClassReplacement(Z)I
    .registers 3

    if-eqz p1, :done
    const-class p0, Ljava/lang/String;

    :done
    invoke-virtual {p0}, Ljava/lang/Object;->hashCode()I
    move-result v0
    return v0
.end method

.method public final checkAfterOtherReference(Ljava/lang/String;Z)I
    .registers 4

    if-eqz p2, :done
    move-object p0, p1

    :done
    invoke-virtual {p0}, Ljava/lang/Object;->hashCode()I
    move-result v0
    return v0
.end method

.method public final checkAfterOtherField(Z)I
    .registers 3

    if-eqz p1, :done
    sget-object p0, Lloops/TestOverwrittenReceiverLoop;->textReplacement:Ljava/lang/String;

    :done
    invoke-virtual {p0}, Ljava/lang/Object;->hashCode()I
    move-result v0
    return v0
.end method

.method public final checkAfterOtherCall(Z)I
    .registers 3

    if-eqz p1, :done
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->getOtherReplacement()Ljava/lang/String;
    move-result-object p0

    :done
    invoke-virtual {p0}, Ljava/lang/Object;->hashCode()I
    move-result v0
    return v0
.end method

.method public final checkAfterOtherConstruction(Z)I
    .registers 3

    if-eqz p1, :done
    new-instance p0, Ljava/lang/StringBuilder;
    invoke-direct {p0}, Ljava/lang/StringBuilder;-><init>()V

    :done
    invoke-virtual {p0}, Ljava/lang/Object;->hashCode()I
    move-result v0
    return v0
.end method

.method public final checkAfterThreeWay(Lloops/TestOverwrittenReceiverLoop;Lloops/TestOverwrittenReceiverLoop;I)I
    .registers 5

    if-eqz p3, :done
    const/4 v0, 0x1
    if-ne p3, v0, :second
    move-object p0, p1
    goto :done

    :second
    move-object p0, p2

    :done
    if-eqz p0, :null_receiver
    const/4 v0, 0x1
    return v0

    :null_receiver
    const/4 v0, 0x2
    return v0
.end method

.method public final checkAfterMixedThreeWay(Ljava/lang/String;Lloops/TestOverwrittenReceiverLoop;I)I
    .registers 5

    if-eqz p3, :done
    const/4 v0, 0x1
    if-ne p3, v0, :second
    move-object p0, p1
    goto :done

    :second
    move-object p0, p2

    :done
    invoke-virtual {p0}, Ljava/lang/Object;->hashCode()I
    move-result v0
    return v0
.end method

.method public final checkAfterNullThreeWay(Ljava/lang/String;I)I
    .registers 4

    if-eqz p2, :done
    const/4 v0, 0x1
    if-ne p2, v0, :replacement
    const/4 p0, 0x0
    goto :done

    :replacement
    move-object p0, p1

    :done
    if-eqz p0, :null_receiver
    const/4 v0, 0x1
    return v0

    :null_receiver
    const/4 v0, 0x2
    return v0
.end method

.method public final checkAfterOptionalReplacement(Ljava/lang/String;I)I
    .registers 4

    if-eqz p2, :done
    const/4 v0, 0x1
    if-ne p2, v0, :done
    move-object p0, p1

    :done
    if-eqz p0, :null_receiver
    const/4 v0, 0x1
    return v0

    :null_receiver
    const/4 v0, 0x2
    return v0
.end method

.method public final checkAfterNestedOptionalReplacement(Ljava/lang/String;I)I
    .registers 4

    if-eqz p2, :done
    const/4 v0, 0x1
    if-ne p2, v0, :inner_done
    move-object p0, p1

    :inner_done
    invoke-virtual {p0}, Ljava/lang/Object;->hashCode()I
    move-result v0

    :done
    if-eqz p0, :null_receiver
    const/4 v0, 0x1
    return v0

    :null_receiver
    const/4 v0, 0x2
    return v0
.end method

.method public final checkAfterNestedSameType(Lloops/TestOverwrittenReceiverLoop;I)I
    .registers 4

    if-eqz p2, :done
    const/4 v0, 0x1
    if-ne p2, v0, :inner_done
    move-object p0, p1

    :inner_done
    invoke-virtual {p0}, Ljava/lang/Object;->hashCode()I
    move-result v0

    :done
    if-eqz p0, :null_receiver
    const/4 v0, 0x1
    return v0

    :null_receiver
    const/4 v0, 0x2
    return v0
.end method

.method public final checkAfterConditionalLoop(Ljava/lang/String;I)I
    .registers 4

    :loop
    if-lez p2, :done
    const/4 v0, 0x1
    if-ne p2, v0, :next
    move-object p0, p1

    :next
    add-int/lit8 p2, p2, -0x1
    goto :loop

    :done
    invoke-virtual {p0}, Ljava/lang/Object;->hashCode()I
    move-result v0
    return v0
.end method

.method public final checkAfterFieldReplacement(Z)I
    .registers 3

    if-eqz p1, :done
    sget-object p0, Lloops/TestOverwrittenReceiverLoop;->replacement:Lloops/TestOverwrittenReceiverLoop;

    :done
    if-eqz p0, :null_receiver
    const/4 v0, 0x1
    return v0

    :null_receiver
    const/4 v0, 0x2
    return v0
.end method

.method public final checkAfterInstanceFieldReplacement(Z)I
    .registers 3

    if-eqz p1, :done
    iget-object p0, p0, Lloops/TestOverwrittenReceiverLoop;->instanceReplacement:Lloops/TestOverwrittenReceiverLoop;

    :done
    if-eqz p0, :null_receiver
    const/4 v0, 0x1
    return v0

    :null_receiver
    const/4 v0, 0x2
    return v0
.end method

.method public final checkAfterCallReplacement(Z)I
    .registers 3

    if-eqz p1, :done
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->getReplacement()Lloops/TestOverwrittenReceiverLoop;
    move-result-object p0

    :done
    if-eqz p0, :null_receiver
    const/4 v0, 0x1
    return v0

    :null_receiver
    const/4 v0, 0x2
    return v0
.end method

.method public final checkAfterReplacement(Lloops/TestOverwrittenReceiverLoop;I)I
    .registers 4

    :loop
    if-lez p2, :done
    move-object p0, p1
    add-int/lit8 p2, p2, -0x1
    goto :loop

    :done
    if-eqz p0, :null_receiver
    const/4 v0, 0x1
    return v0

    :null_receiver
    const/4 v0, 0x2
    return v0
.end method

.method public final checkAfterReceiverUse(I)I
    .registers 3

    invoke-virtual {p0}, Ljava/lang/Object;->hashCode()I
    move-result v0

    :loop
    if-lez p1, :done
    const/4 p0, 0x0
    add-int/lit8 p1, p1, -0x1
    goto :loop

    :done
    if-eqz p0, :null_receiver
    return v0

    :null_receiver
    const/4 v0, 0x2
    return v0
.end method

.method public final checkAfterCast(Z)I
    .registers 3

    if-eqz p1, :done
    check-cast p0, Ljava/lang/Cloneable;

    :done
    invoke-virtual {p0}, Ljava/lang/Object;->hashCode()I
    move-result v0
    return v0
.end method

.method public final checkAfterArrayGet([Ljava/lang/String;Z)I
    .registers 5

    if-eqz p2, :done
    const/4 v0, 0x0
    aget-object p0, p1, v0

    :done
    invoke-virtual {p0}, Ljava/lang/Object;->hashCode()I
    move-result v0
    return v0
.end method

.method public final checkAfterFilledArray(Z)I
    .registers 4

    if-eqz p1, :done
    const/4 v0, 0x1
    filled-new-array {v0}, [I
    move-result-object p0

    :done
    invoke-virtual {p0}, Ljava/lang/Object;->hashCode()I
    move-result v0
    return v0
.end method

.method public final checkAfterArrayMove([Ljava/lang/String;Z)I
    .registers 5

    if-eqz p2, :done
    const/4 v0, 0x0
    aget-object v0, p1, v0
    move-object p0, v0

    :done
    invoke-virtual {p0}, Ljava/lang/Object;->hashCode()I
    move-result v0
    return v0
.end method

.method public final checkAfterNullMove(Z)I
    .registers 3

    const/4 v0, 0x0
    if-eqz p1, :done
    move-object p0, v0

    :done
    if-eqz p0, :null_receiver
    const/4 v0, 0x1
    return v0

    :null_receiver
    const/4 v0, 0x2
    return v0
.end method

.method public final checkAfterSelfMove(Z)I
    .registers 3

    if-eqz p1, :done
    move-object p0, p0

    :done
    invoke-virtual {p0}, Ljava/lang/Object;->hashCode()I
    move-result v0
    return v0
.end method

.method public final checkAfterLoopSelfMove(I)I
    .registers 3

    :loop
    if-lez p1, :done
    const/4 p0, 0x0
    move-object p0, p0
    add-int/lit8 p1, p1, -0x1
    goto :loop

    :done
    if-eqz p0, :null_receiver
    const/4 v0, 0x1
    return v0

    :null_receiver
    const/4 v0, 0x2
    return v0
.end method

.method public final checkAfterCaughtCast(Ljava/lang/Object;Z)I
    .registers 5

    if-eqz p2, :done

    :try_start
    check-cast p1, Ljava/lang/String;
    move-object p0, p1
    :try_end
    goto :done

    .catch Ljava/lang/ClassCastException; {:try_start .. :try_end} :catch

    :catch
    move-exception v0

    :done
    invoke-virtual {p0}, Ljava/lang/Object;->hashCode()I
    move-result v0
    return v0
.end method

.method public final checkAfterThrowBeforeMove(Ljava/lang/Object;Z)I
    .registers 5

    if-eqz p2, :done

    :try_start
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    move-object p0, p1
    :try_end
    goto :done

    .catch Ljava/lang/RuntimeException; {:try_start .. :try_end} :catch

    :catch
    move-exception v0

    :done
    invoke-virtual {p0}, Ljava/lang/Object;->hashCode()I
    move-result v0
    return v0
.end method

.method public final checkAfterThrowAfterMove(Ljava/lang/Object;Z)I
    .registers 5

    if-eqz p2, :done

    :try_start
    move-object p0, p1
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end
    goto :done

    .catch Ljava/lang/RuntimeException; {:try_start .. :try_end} :catch

    :catch
    move-exception v0

    :done
    invoke-virtual {p0}, Ljava/lang/Object;->hashCode()I
    move-result v0
    return v0
.end method

.method public final checkAfterTwoThrowSites(Ljava/lang/Object;Z)I
    .registers 5

    if-eqz p2, :done

    :try_start
    check-cast p1, Ljava/lang/String;
    move-object p0, p1
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end
    goto :done

    .catch Ljava/lang/Exception; {:try_start .. :try_end} :catch

    :catch
    move-exception v0

    :done
    invoke-virtual {p0}, Ljava/lang/Object;->hashCode()I
    move-result v0
    return v0
.end method

.method public final checkAfterThreeThrowStates(Ljava/lang/Object;Ljava/lang/String;Z)I
    .registers 6

    if-eqz p3, :done

    :try_start
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    move-object p0, p1
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    move-object p0, p2
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end
    goto :done

    .catch Ljava/lang/RuntimeException; {:try_start .. :try_end} :catch

    :catch
    move-exception v0

    :done
    invoke-virtual {p0}, Ljava/lang/Object;->hashCode()I
    move-result v0
    return v0
.end method

.method public final checkAfterBranchedThrow(Ljava/lang/Object;Z)I
    .registers 5

    :try_start
    if-eqz p2, :skip_move
    move-object p0, p1

    :skip_move
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end
    goto :done

    .catch Ljava/lang/RuntimeException; {:try_start .. :try_end} :catch

    :catch
    move-exception v0

    :done
    invoke-virtual {p0}, Ljava/lang/Object;->hashCode()I
    move-result v0
    return v0
.end method

.method public final checkAfterBranchedThrowWithOutsideCall(Ljava/lang/Object;Z)I
    .registers 5

    if-eqz p2, :skip_move
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    move-object p0, p1

    :skip_move
    :try_start
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end
    goto :done

    .catch Ljava/lang/RuntimeException; {:try_start .. :try_end} :catch

    :catch
    move-exception v0

    :done
    invoke-virtual {p0}, Ljava/lang/Object;->hashCode()I
    move-result v0
    return v0
.end method

.method public final checkAfterBranchedThrowWithHandler(Ljava/lang/Object;Z)I
    .registers 5

    :try_start
    if-eqz p2, :skip_move
    move-object p0, p1

    :skip_move
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end
    goto :done

    .catch Ljava/lang/RuntimeException; {:try_start .. :try_end} :catch

    :catch
    move-exception v0
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->recordCatch()V

    :done
    invoke-virtual {p0}, Ljava/lang/Object;->hashCode()I
    move-result v0
    return v0
.end method

.method public final checkAfterBranchedThrowWithHandlerReplacement(Ljava/lang/Object;Ljava/lang/Object;Z)I
    .registers 6

    :try_start
    if-eqz p3, :skip_move
    move-object p0, p1

    :skip_move
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end
    goto :done

    .catch Ljava/lang/RuntimeException; {:try_start .. :try_end} :catch

    :catch
    move-exception v0
    move-object p0, p2

    :done
    invoke-virtual {p0}, Ljava/lang/Object;->hashCode()I
    move-result v0
    return v0
.end method

.method public final checkAfterTwoThrowSitesWithHandler(Ljava/lang/Object;Ljava/lang/Object;Z)I
    .registers 6

    if-eqz p3, :done

    :try_start
    check-cast p1, Ljava/lang/String;
    move-object p0, p1
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end
    goto :done

    .catch Ljava/lang/Exception; {:try_start .. :try_end} :catch

    :catch
    move-exception v0
    move-object p0, p2

    :done
    invoke-virtual {p0}, Ljava/lang/Object;->hashCode()I
    move-result v0
    return v0
.end method

.method public final checkAfterProtectedBranchCondition([Ljava/lang/Object;Ljava/lang/Object;)I
    .registers 5

    :try_start
    array-length v0, p1
    if-eqz v0, :skip_move
    move-object p0, p2

    :skip_move
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end
    goto :done

    .catch Ljava/lang/Exception; {:try_start .. :try_end} :catch

    :catch
    move-exception v0
    move-object p0, p2

    :done
    invoke-virtual {p0}, Ljava/lang/Object;->hashCode()I
    move-result v0
    return v0
.end method

.method public final checkAfterBranchedThrowCatchReturn(Ljava/lang/Object;Z)I
    .registers 5

    :try_start
    if-eqz p2, :skip_move
    move-object p0, p1

    :skip_move
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end
    goto :done

    .catch Ljava/lang/RuntimeException; {:try_start .. :try_end} :catch

    :catch
    move-exception v0
    const/4 v0, -0x1
    return v0

    :done
    invoke-virtual {p0}, Ljava/lang/Object;->hashCode()I
    move-result v0
    return v0
.end method

.method public final checkAfterBranchedTwoProtectedCallsCatchReturn(Ljava/lang/Object;Z)I
    .registers 5

    :try_start
    if-eqz p2, :skip_move
    move-object p0, p1
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V

    :skip_move
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end
    goto :done

    .catch Ljava/lang/RuntimeException; {:try_start .. :try_end} :catch

    :catch
    move-exception v0
    const/4 v0, -0x1
    return v0

    :done
    invoke-virtual {p0}, Ljava/lang/Object;->hashCode()I
    move-result v0
    return v0
.end method

.method public final checkAfterSplitProtectedCallsWithUnprotectedPrelude(Ljava/lang/Object;Z)I
    .registers 5

    if-eqz p2, :resume

    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0
    :try_start_call
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_call
    goto :done

    :resume
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0
    :try_start_resume
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_resume

    :done
    return v0

    :catch
    move-exception v1
    const/4 v0, -0x1
    return v0

    .catch Ljava/lang/RuntimeException; {:try_start_call .. :try_end_call} :catch
    .catch Ljava/lang/RuntimeException; {:try_start_resume .. :try_end_resume} :catch
.end method

.method public final checkAfterNestedSplitProtectedCalls(Ljava/lang/Object;ZZ)I
    .registers 6

    if-eqz p2, :else_branch

    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0
    if-eqz p3, :done

    :try_start_then
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_then
    goto :done

    :else_branch
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0
    :try_start_else
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_else

    :done
    return v0

    :catch
    move-exception v1
    const/4 v0, -0x1
    return v0

    .catch Ljava/lang/RuntimeException; {:try_start_then .. :try_end_then} :catch
    .catch Ljava/lang/RuntimeException; {:try_start_else .. :try_end_else} :catch
.end method

.method public final checkLoopProtectedCallWithUnprotectedPrelude(Ljava/lang/Object;I)I
    .registers 5

    :loop
    if-lez p2, :done
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0

    :try_start
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end
    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v0
    add-int/lit8 p2, p2, -0x1
    goto :loop

    :done
    return p2

    :catch
    move-exception v1
    const/4 v0, -0x1
    return v0

    .catch Ljava/lang/RuntimeException; {:try_start .. :try_end} :catch
.end method

.method public final checkLoopSplitProtectedCallsWithUnprotectedMiddle(Ljava/lang/Object;I)I
    .registers 5

    :loop
    if-lez p2, :done
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0

    :try_start_first
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_first

    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v0

    :try_start_second
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_second

    add-int/lit8 p2, p2, -0x1
    goto :loop

    :done
    return p2

    :catch
    move-exception v1
    const/4 v0, -0x1
    return v0

    .catch Ljava/lang/RuntimeException; {:try_start_first .. :try_end_first} :catch
    .catch Ljava/lang/RuntimeException; {:try_start_second .. :try_end_second} :catch
.end method

.method public final checkDoWhileProtectedCallWithUnprotectedTail(Ljava/lang/Object;I)I
    .registers 5

    :loop
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0

    :try_start
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end

    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v0
    add-int/lit8 p2, p2, -0x1
    if-gtz p2, :loop
    return p2

    :catch
    move-exception v1
    const/4 v0, -0x1
    return v0

    .catch Ljava/lang/RuntimeException; {:try_start .. :try_end} :catch
.end method

.method public final checkDoWhileSplitProtectedCalls(Ljava/lang/Object;I)I
    .registers 5

    :loop
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0

    :try_start_first
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_first

    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v0

    :try_start_second
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_second

    add-int/lit8 p2, p2, -0x1
    if-gtz p2, :loop
    return p2

    :catch
    move-exception v1
    const/4 v0, -0x1
    return v0

    .catch Ljava/lang/RuntimeException; {:try_start_first .. :try_end_first} :catch
    .catch Ljava/lang/RuntimeException; {:try_start_second .. :try_end_second} :catch
.end method

.method public final checkNestedLoopProtectedCallWithUnprotectedTail(Ljava/lang/Object;II)I
    .registers 7

    :outer
    if-lez p2, :done
    move v2, p3

    :inner
    if-lez v2, :after_inner
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0

    :try_start
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end

    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v0
    add-int/lit8 v2, v2, -0x1
    goto :inner

    :after_inner
    add-int/lit8 p2, p2, -0x1
    goto :outer

    :done
    return p2

    :catch
    move-exception v1
    const/4 v0, -0x1
    return v0

    .catch Ljava/lang/RuntimeException; {:try_start .. :try_end} :catch
.end method

.method public final checkNestedLoopSplitProtectedCalls(Ljava/lang/Object;II)I
    .registers 7

    :outer
    if-lez p2, :done
    move v2, p3

    :inner
    if-lez v2, :after_inner
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0

    :try_start_first
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_first

    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v0

    :try_start_second
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_second

    add-int/lit8 v2, v2, -0x1
    goto :inner

    :after_inner
    add-int/lit8 p2, p2, -0x1
    goto :outer

    :done
    return p2

    :catch
    move-exception v1
    const/4 v0, -0x1
    return v0

    .catch Ljava/lang/RuntimeException; {:try_start_first .. :try_end_first} :catch
    .catch Ljava/lang/RuntimeException; {:try_start_second .. :try_end_second} :catch
.end method

.method public final checkOuterAndInnerLoopProtectedCalls(Ljava/lang/Object;II)I
    .registers 7

    :outer
    if-lez p2, :done
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0

    :try_start_outer
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_outer

    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v0
    move v2, p3

    :inner
    if-lez v2, :after_inner
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0

    :try_start_inner
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_inner

    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v0
    add-int/lit8 v2, v2, -0x1
    goto :inner

    :after_inner
    add-int/lit8 p2, p2, -0x1
    goto :outer

    :done
    return p2

    :catch
    move-exception v1
    const/4 v0, -0x1
    return v0

    .catch Ljava/lang/RuntimeException; {:try_start_outer .. :try_end_outer} :catch
    .catch Ljava/lang/RuntimeException; {:try_start_inner .. :try_end_inner} :catch
.end method

.method public final checkSiblingInnerLoopProtectedCalls(Ljava/lang/Object;II)I
    .registers 7

    :outer
    if-lez p2, :done
    move v2, p3

    :first_inner
    if-lez v2, :between
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0

    :try_start_first
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_first

    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v0
    add-int/lit8 v2, v2, -0x1
    goto :first_inner

    :between
    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v0
    move v2, p3

    :second_inner
    if-lez v2, :after_second
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0

    :try_start_second
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_second

    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v0
    add-int/lit8 v2, v2, -0x1
    goto :second_inner

    :after_second
    add-int/lit8 p2, p2, -0x1
    goto :outer

    :done
    return p2

    :catch
    move-exception v1
    const/4 v0, -0x1
    return v0

    .catch Ljava/lang/RuntimeException; {:try_start_first .. :try_end_first} :catch
    .catch Ljava/lang/RuntimeException; {:try_start_second .. :try_end_second} :catch
.end method

.method public final checkConditionalLoopProtectedCall(Ljava/lang/Object;IZ)I
    .registers 6

    :loop
    if-lez p2, :done
    if-eqz p3, :else_branch

    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0

    :try_start
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end

    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v0
    goto :after_branch

    :else_branch
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0

    :after_branch
    add-int/lit8 p2, p2, -0x1
    goto :loop

    :done
    return p2

    :catch
    move-exception v1
    const/4 v0, -0x1
    return v0

    .catch Ljava/lang/RuntimeException; {:try_start .. :try_end} :catch
.end method

.method public final checkLoopSplitProtectedBranches(Ljava/lang/Object;IZ)I
    .registers 6

    :loop
    if-lez p2, :done
    if-eqz p3, :else_branch

    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0

    :try_start_then
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_then

    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v0
    goto :after_branch

    :else_branch
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0

    :try_start_else
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_else

    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v0

    :after_branch
    add-int/lit8 p2, p2, -0x1
    goto :loop

    :done
    return p2

    :catch
    move-exception v1
    const/4 v0, -0x1
    return v0

    .catch Ljava/lang/RuntimeException; {:try_start_then .. :try_end_then} :catch
    .catch Ljava/lang/RuntimeException; {:try_start_else .. :try_end_else} :catch
.end method

.method public final checkLoopProtectedSwitchCase(Ljava/lang/Object;II)I
    .registers 6

    :loop
    if-lez p2, :done
    packed-switch p3, :switch_data

    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0
    goto :after_switch

    :case_protected
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0

    :try_start
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end

    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v0
    goto :after_switch

    :case_unprotected
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0

    :after_switch
    add-int/lit8 p2, p2, -0x1
    goto :loop

    :done
    return p2

    :catch
    move-exception v1
    const/4 v0, -0x1
    return v0

    :switch_data
    .packed-switch 0x0
        :case_protected
        :case_unprotected
    .end packed-switch

    .catch Ljava/lang/RuntimeException; {:try_start .. :try_end} :catch
.end method

.method public final checkLoopSplitProtectedSwitchCases(Ljava/lang/Object;II)I
    .registers 6

    :loop
    if-lez p2, :done
    packed-switch p3, :switch_data

    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0
    goto :after_switch

    :case_first
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0

    :try_start_first
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_first

    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v0
    goto :after_switch

    :case_second
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0

    :try_start_second
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_second

    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v0

    :after_switch
    add-int/lit8 p2, p2, -0x1
    goto :loop

    :done
    return p2

    :catch
    move-exception v1
    const/4 v0, -0x1
    return v0

    :switch_data
    .packed-switch 0x0
        :case_first
        :case_second
    .end packed-switch

    .catch Ljava/lang/RuntimeException; {:try_start_first .. :try_end_first} :catch
    .catch Ljava/lang/RuntimeException; {:try_start_second .. :try_end_second} :catch
.end method

.method public final checkLoopProtectedSwitchWithTerminalCase(Ljava/lang/Object;II)I
    .registers 6

    :loop
    if-lez p2, :done
    packed-switch p3, :switch_data

    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0
    goto :after_switch

    :case_protected
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0

    :try_start
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end

    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v0
    goto :after_switch

    :case_terminal
    return p2

    :after_switch
    add-int/lit8 p2, p2, -0x1
    goto :loop

    :done
    return p2

    :catch
    move-exception v1
    const/4 v0, -0x1
    return v0

    :switch_data
    .packed-switch 0x0
        :case_protected
        :case_terminal
    .end packed-switch

    .catch Ljava/lang/RuntimeException; {:try_start .. :try_end} :catch
.end method

.method public final checkDoWhileProtectedSwitchWithUpdate(Ljava/lang/Object;II)I
    .registers 6

    :loop
    packed-switch p3, :switch_data

    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0
    goto :after_switch

    :case_protected
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0

    :try_start
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end

    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v0
    goto :after_switch

    :case_unprotected
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0

    :after_switch
    add-int/lit8 p2, p2, -0x1
    if-gtz p2, :loop
    return p2

    :catch
    move-exception v1
    const/4 v0, -0x1
    return v0

    :switch_data
    .packed-switch 0x0
        :case_protected
        :case_unprotected
    .end packed-switch

    .catch Ljava/lang/RuntimeException; {:try_start .. :try_end} :catch
.end method

.method public final checkLoopProtectedSwitchWithDirectContinue(Ljava/lang/Object;II)I
    .registers 6

    :loop
    if-lez p2, :done
    packed-switch p3, :switch_data

    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0
    goto :after_switch

    :case_continue
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0
    goto :loop

    :case_protected
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0

    :try_start
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end

    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v0

    :after_switch
    add-int/lit8 p2, p2, -0x1
    goto :loop

    :done
    return p2

    :catch
    move-exception v1
    const/4 v0, -0x1
    return v0

    :switch_data
    .packed-switch 0x0
        :case_continue
        :case_protected
    .end packed-switch

    .catch Ljava/lang/RuntimeException; {:try_start .. :try_end} :catch
.end method

.method public final checkLoopSwitchConditionalProtectedCall(Ljava/lang/Object;IIZ)I
    .registers 7

    :loop
    if-lez p2, :done
    packed-switch p3, :switch_data

    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0
    goto :after_switch

    :case_conditional
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0
    if-eqz p4, :after_protected

    :try_start
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end

    :after_protected
    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v0
    goto :after_switch

    :case_plain
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0

    :after_switch
    add-int/lit8 p2, p2, -0x1
    goto :loop

    :done
    return p2

    :catch
    move-exception v1
    const/4 v0, -0x1
    return v0

    :switch_data
    .packed-switch 0x0
        :case_conditional
        :case_plain
    .end packed-switch

    .catch Ljava/lang/RuntimeException; {:try_start .. :try_end} :catch
.end method

.method public final checkLoopSplitProtectedSparseSwitchCases(Ljava/lang/Object;II)I
    .registers 6

    :loop
    if-lez p2, :done
    sparse-switch p3, :switch_data

    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0
    goto :after_switch

    :case_first
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0

    :try_start_first
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_first

    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v0
    goto :after_switch

    :case_second
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0

    :try_start_second
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_second

    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v0

    :after_switch
    add-int/lit8 p2, p2, -0x1
    goto :loop

    :done
    return p2

    :catch
    move-exception v1
    const/4 v0, -0x1
    return v0

    :switch_data
    .sparse-switch
        0x2 -> :case_first
        0x10 -> :case_second
    .end sparse-switch

    .catch Ljava/lang/RuntimeException; {:try_start_first .. :try_end_first} :catch
    .catch Ljava/lang/RuntimeException; {:try_start_second .. :try_end_second} :catch
.end method

.method public final checkLoopNestedSwitchSplitProtectedCases(Ljava/lang/Object;III)I
    .registers 7

    :loop
    if-lez p2, :done
    packed-switch p3, :outer_switch_data

    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0
    goto :after_outer

    :outer_case
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0
    sparse-switch p4, :inner_switch_data

    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0

    :try_start_default
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_default

    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v0
    goto :after_inner

    :inner_case
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0

    :try_start_first
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_first

    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v0

    :after_inner
    goto :after_outer

    :after_outer
    add-int/lit8 p2, p2, -0x1
    goto :loop

    :done
    return p2

    :catch
    move-exception v1
    const/4 v0, -0x1
    return v0

    :outer_switch_data
    .packed-switch 0x0
        :outer_case
    .end packed-switch

    :inner_switch_data
    .sparse-switch
        0x4 -> :inner_case
    .end sparse-switch

    .catch Ljava/lang/RuntimeException; {:try_start_first .. :try_end_first} :catch
    .catch Ljava/lang/RuntimeException; {:try_start_default .. :try_end_default} :catch
.end method

.method public final checkLoopConditionalNestedSwitch(Ljava/lang/Object;IIIZ)I
    .registers 8

    :loop
    if-lez p2, :done
    packed-switch p3, :outer_switch_data

    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0
    goto :after_outer

    :outer_case
    if-eqz p5, :plain_branch
    sparse-switch p4, :inner_switch_data

    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0
    goto :after_outer

    :inner_case
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0

    :try_start
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end

    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v0
    goto :after_outer

    :plain_branch
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0

    :after_outer
    add-int/lit8 p2, p2, -0x1
    goto :loop

    :done
    return p2

    :catch
    move-exception v1
    const/4 v0, -0x1
    return v0

    :outer_switch_data
    .packed-switch 0x0
        :outer_case
    .end packed-switch

    :inner_switch_data
    .sparse-switch
        0x5 -> :inner_case
    .end sparse-switch

    .catch Ljava/lang/RuntimeException; {:try_start .. :try_end} :catch
.end method

.method public final checkLoopNestedSwitchContinue(Ljava/lang/Object;III)I
    .registers 7

    :loop
    if-lez p2, :done
    packed-switch p3, :outer_switch_data

    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0
    goto :after_outer

    :outer_case
    sparse-switch p4, :inner_switch_data

    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0
    goto :after_outer

    :inner_continue
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0
    goto :loop

    :after_outer
    add-int/lit8 p2, p2, -0x1
    goto :loop

    :done
    return p2

    :outer_switch_data
    .packed-switch 0x0
        :outer_case
    .end packed-switch

    :inner_switch_data
    .sparse-switch
        0x6 -> :inner_continue
    .end sparse-switch
.end method

.method public final checkLoopNestedSwitchTerminal(Ljava/lang/Object;III)I
    .registers 7

    :loop
    if-lez p2, :done
    packed-switch p3, :outer_switch_data

    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0
    goto :after_outer

    :outer_case
    sparse-switch p4, :inner_switch_data

    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0
    goto :after_outer

    :inner_return
    return p2

    :after_outer
    add-int/lit8 p2, p2, -0x1
    goto :loop

    :done
    return p2

    :outer_switch_data
    .packed-switch 0x0
        :outer_case
    .end packed-switch

    :inner_switch_data
    .sparse-switch
        0x7 -> :inner_return
    .end sparse-switch
.end method

.method public final checkLoopNestedSwitchOuterFallThrough(Ljava/lang/Object;III)I
    .registers 7

    :loop
    if-lez p2, :done
    packed-switch p3, :outer_switch_data

    :outer_default
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0
    goto :after_outer

    :outer_case
    sparse-switch p4, :inner_switch_data

    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0
    goto :outer_default

    :inner_case
    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v0
    goto :outer_default

    :after_outer
    add-int/lit8 p2, p2, -0x1
    goto :loop

    :done
    return p2

    :outer_switch_data
    .packed-switch 0x0
        :outer_case
    .end packed-switch

    :inner_switch_data
    .sparse-switch
        0x8 -> :inner_case
    .end sparse-switch
.end method

.method public final checkLoopSwitchCatchContinues(Ljava/lang/Object;II)I
    .registers 6

    :loop
    if-lez p2, :done
    packed-switch p3, :switch_data

    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0
    goto :after_switch

    :case_protected
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0

    :try_start
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end
    goto :after_protected

    :catch
    move-exception v1
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->recordCatch()V

    :after_protected
    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v0

    :after_switch
    add-int/lit8 p2, p2, -0x1
    goto :loop

    :done
    return p2

    :switch_data
    .packed-switch 0x0
        :case_protected
    .end packed-switch

    .catch Ljava/lang/RuntimeException; {:try_start .. :try_end} :catch
.end method

.method public final checkLoopSwitchSplitCatchToUpdate(Ljava/lang/Object;II)I
    .registers 6

    :loop
    if-lez p2, :done
    packed-switch p3, :switch_data

    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0
    goto :after_switch

    :case_protected
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0

    :try_start_first
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_first

    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v0

    :try_start_second
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_second

    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v0

    :after_switch
    add-int/lit8 p2, p2, -0x1
    goto :loop

    :done
    return p2

    :catch
    move-exception v1
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->recordCatch()V
    goto :after_switch

    :switch_data
    .packed-switch 0x0
        :case_protected
    .end packed-switch

    .catch Ljava/lang/RuntimeException; {:try_start_first .. :try_end_first} :catch
    .catch Ljava/lang/RuntimeException; {:try_start_second .. :try_end_second} :catch
.end method

.method public final checkLoopSwitchCasesCatchToUpdate(Ljava/lang/Object;II)I
    .registers 6

    :loop
    if-lez p2, :done
    packed-switch p3, :switch_data

    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0
    goto :after_switch

    :case_first
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0
    :try_start_first
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_first
    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v0
    goto :after_switch

    :case_second
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0
    :try_start_second
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_second
    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v0

    :after_switch
    add-int/lit8 p2, p2, -0x1
    goto :loop

    :done
    return p2

    :catch
    move-exception v1
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->recordCatch()V
    goto :after_switch

    :switch_data
    .packed-switch 0x0
        :case_first
        :case_second
    .end packed-switch

    .catch Ljava/lang/RuntimeException; {:try_start_first .. :try_end_first} :catch
    .catch Ljava/lang/RuntimeException; {:try_start_second .. :try_end_second} :catch
.end method

.method public final checkSwitchCasesCatchToReturn(Ljava/lang/Object;I)I
    .registers 5

    packed-switch p2, :switch_data

    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0
    goto :after_switch

    :case_first
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0
    :try_start_first
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_first
    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v0
    goto :after_switch

    :case_second
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0
    :try_start_second
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_second
    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v0

    :after_switch
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0
    return p2

    :catch
    move-exception v1
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->recordCatch()V
    goto :after_switch

    :switch_data
    .packed-switch 0x0
        :case_first
        :case_second
    .end packed-switch

    .catch Ljava/lang/RuntimeException; {:try_start_first .. :try_end_first} :catch
    .catch Ljava/lang/RuntimeException; {:try_start_second .. :try_end_second} :catch
.end method

.method public final checkSwitchCasesCatchDirectReturn(Ljava/lang/Object;I)I
    .registers 5

    packed-switch p2, :switch_data

    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0
    goto :after_switch

    :case_first
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0
    :try_start_first
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_first
    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v0
    goto :after_switch

    :case_second
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0
    :try_start_second
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_second
    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v0

    :after_switch
    return p2

    :catch
    move-exception v1
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->recordCatch()V
    goto :after_switch

    :switch_data
    .packed-switch 0x0
        :case_first
        :case_second
    .end packed-switch

    .catch Ljava/lang/RuntimeException; {:try_start_first .. :try_end_first} :catch
    .catch Ljava/lang/RuntimeException; {:try_start_second .. :try_end_second} :catch
.end method

.method public final checkSwitchCasesCatchSharedThrow(Ljava/lang/RuntimeException;I)I
    .registers 5

    packed-switch p2, :switch_data

    invoke-virtual {p1}, Ljava/lang/RuntimeException;->hashCode()I
    move-result v0
    goto :after_switch

    :case_first
    invoke-virtual {p1}, Ljava/lang/RuntimeException;->hashCode()I
    move-result v0
    :try_start_first
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_first
    invoke-virtual {p1}, Ljava/lang/RuntimeException;->toString()Ljava/lang/String;
    move-result-object v0
    goto :after_switch

    :case_second
    invoke-virtual {p1}, Ljava/lang/RuntimeException;->hashCode()I
    move-result v0
    :try_start_second
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_second
    invoke-virtual {p1}, Ljava/lang/RuntimeException;->toString()Ljava/lang/String;
    move-result-object v0

    :after_switch
    throw p1

    :catch
    move-exception v1
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->recordCatch()V
    goto :after_switch

    :switch_data
    .packed-switch 0x0
        :case_first
        :case_second
    .end packed-switch

    .catch Ljava/lang/RuntimeException; {:try_start_first .. :try_end_first} :catch
    .catch Ljava/lang/RuntimeException; {:try_start_second .. :try_end_second} :catch
.end method

.method public final checkSwitchCasesCatchResult(Ljava/lang/Object;I)I
    .registers 6

    packed-switch p2, :switch_data

    const/4 v0, 0x2
    goto :done

    :case_first
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v1
    :try_start_first
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_first
    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v1
    const/4 v0, 0x0
    goto :done

    :case_second
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v1
    :try_start_second
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_second
    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v1
    const/4 v0, 0x1

    :done
    return v0

    :catch
    move-exception v2
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->recordCatch()V
    const/4 v0, -0x1
    goto :done

    :switch_data
    .packed-switch 0x0
        :case_first
        :case_second
    .end packed-switch

    .catch Ljava/lang/RuntimeException; {:try_start_first .. :try_end_first} :catch
    .catch Ljava/lang/RuntimeException; {:try_start_second .. :try_end_second} :catch
.end method

.method public final checkSwitchCasesCatchArgumentResult(Ljava/lang/Object;II)I
    .registers 7

    packed-switch p2, :switch_data

    const/4 v0, 0x2
    goto :done

    :case_first
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v1
    :try_start_first
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_first
    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v1
    const/4 v0, 0x0
    goto :done

    :case_second
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v1
    :try_start_second
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_second
    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v1
    const/4 v0, 0x1

    :done
    return v0

    :catch
    move-exception v2
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->recordCatch()V
    move v0, p3
    goto :done

    :switch_data
    .packed-switch 0x0
        :case_first
        :case_second
    .end packed-switch

    .catch Ljava/lang/RuntimeException; {:try_start_first .. :try_end_first} :catch
    .catch Ljava/lang/RuntimeException; {:try_start_second .. :try_end_second} :catch
.end method

.method public final checkSwitchCasesCatchBranchValue(Ljava/lang/Object;I)I
    .registers 6

    packed-switch p2, :switch_data

    const/4 v0, 0x2
    goto :done

    :case_first
    const/4 v0, 0x4
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v1
    :try_start_first
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_first
    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v1
    const/4 v0, 0x0
    goto :done

    :case_second
    const/4 v0, 0x5
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v1
    :try_start_second
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_second
    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v1
    const/4 v0, 0x1

    :done
    return v0

    :catch
    move-exception v2
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->recordCatch()V
    goto :done

    :switch_data
    .packed-switch 0x0
        :case_first
        :case_second
    .end packed-switch

    .catch Ljava/lang/RuntimeException; {:try_start_first .. :try_end_first} :catch
    .catch Ljava/lang/RuntimeException; {:try_start_second .. :try_end_second} :catch
.end method

.method public final checkSwitchCasesCatchComputedValue(Ljava/lang/Object;I)I
    .registers 6

    packed-switch p2, :switch_data

    const/4 v0, 0x2
    goto :done

    :case_first
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0
    :try_start_first
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_first
    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v1
    const/4 v0, 0x0
    goto :done

    :case_second
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0
    :try_start_second
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_second
    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v1
    const/4 v0, 0x1

    :done
    return v0

    :catch
    move-exception v2
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->recordCatch()V
    goto :done

    :switch_data
    .packed-switch 0x0
        :case_first
        :case_second
    .end packed-switch

    .catch Ljava/lang/RuntimeException; {:try_start_first .. :try_end_first} :catch
    .catch Ljava/lang/RuntimeException; {:try_start_second .. :try_end_second} :catch
.end method

.method public final checkSwitchSplitCatchUpdatedValue(Ljava/lang/Object;I)I
    .registers 6

    packed-switch p2, :switch_data

    const/4 v0, 0x2
    goto :done

    :case_first
    const/4 v0, 0x4
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v1
    :try_start_first_a
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_first_a
    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v1
    const/4 v0, 0x6
    :try_start_first_b
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_first_b
    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v1
    const/4 v0, 0x0
    goto :done

    :case_second
    const/4 v0, 0x5
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v1
    :try_start_second_a
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_second_a
    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v1
    const/4 v0, 0x7
    :try_start_second_b
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_second_b
    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v1
    const/4 v0, 0x1

    :done
    return v0

    :catch
    move-exception v2
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->recordCatch()V
    goto :done

    :switch_data
    .packed-switch 0x0
        :case_first
        :case_second
    .end packed-switch

    .catch Ljava/lang/RuntimeException; {:try_start_first_a .. :try_end_first_a} :catch
    .catch Ljava/lang/RuntimeException; {:try_start_first_b .. :try_end_first_b} :catch
    .catch Ljava/lang/RuntimeException; {:try_start_second_a .. :try_end_second_a} :catch
    .catch Ljava/lang/RuntimeException; {:try_start_second_b .. :try_end_second_b} :catch
.end method

.method public final checkSwitchCatchUpdatedInsideTry(Ljava/lang/Object;I)I
    .registers 6

    packed-switch p2, :switch_data

    const/4 v0, 0x2
    goto :done

    :case_first
    const/4 v0, 0x4
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v1
    :try_start_first
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    const/4 v0, 0x6
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_first
    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v1
    const/4 v0, 0x0
    goto :done

    :case_second
    const/4 v0, 0x5
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v1
    :try_start_second
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    const/4 v0, 0x7
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_second
    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v1
    const/4 v0, 0x1

    :done
    return v0

    :catch
    move-exception v2
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->recordCatch()V
    goto :done

    :switch_data
    .packed-switch 0x0
        :case_first
        :case_second
    .end packed-switch

    .catch Ljava/lang/RuntimeException; {:try_start_first .. :try_end_first} :catch
    .catch Ljava/lang/RuntimeException; {:try_start_second .. :try_end_second} :catch
.end method

.method public final checkSwitchCatchThrowingAssignment(Ljava/lang/Object;I)I
    .registers 6

    packed-switch p2, :switch_data

    const/4 v0, 0x2
    goto :done

    :case_first
    const/4 v0, 0x4
    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v1
    :try_start_first
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_first
    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v1
    const/4 v0, 0x0
    goto :done

    :case_second
    const/4 v0, 0x5
    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v1
    :try_start_second
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_second
    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v1
    const/4 v0, 0x1

    :done
    return v0

    :catch
    move-exception v2
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->recordCatch()V
    goto :done

    :switch_data
    .packed-switch 0x0
        :case_first
        :case_second
    .end packed-switch

    .catch Ljava/lang/RuntimeException; {:try_start_first .. :try_end_first} :catch
    .catch Ljava/lang/RuntimeException; {:try_start_second .. :try_end_second} :catch
.end method

.method public final checkSwitchCatchArgumentOrBranchValue(Ljava/lang/Object;II)I
    .registers 7

    packed-switch p2, :switch_data
    goto :done

    :case_first
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0
    :try_start_first
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_first
    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v0
    const/4 p3, 0x0
    goto :done

    :case_second
    const/4 p3, 0x5
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I
    move-result v0
    :try_start_second
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->mayThrow()V
    :try_end_second
    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object v0
    const/4 p3, 0x1

    :done
    return p3

    :catch
    move-exception v1
    invoke-static {}, Lloops/TestOverwrittenReceiverLoop;->recordCatch()V
    goto :done

    :switch_data
    .packed-switch 0x0
        :case_first
        :case_second
    .end packed-switch

    .catch Ljava/lang/RuntimeException; {:try_start_first .. :try_end_first} :catch
    .catch Ljava/lang/RuntimeException; {:try_start_second .. :try_end_second} :catch
.end method
