.class public Ltrycatch/TestSharedTypedHandlerRanges;
.super Ljava/lang/Object;

.method public static test(Z)V
    .locals 1
    if-eqz p0, :second

    :first_start
    invoke-static {}, Ltrycatch/TestSharedTypedHandlerRanges;->first()V
    :first_end
    .catch Ljava/lang/IllegalArgumentException; {:first_start .. :first_end} :handler
    goto :done

    :second
    :second_start
    invoke-static {}, Ltrycatch/TestSharedTypedHandlerRanges;->second()V
    :second_end
    .catch Ljava/lang/IllegalStateException; {:second_start .. :second_end} :handler

    :done
    return-void

    :handler
    move-exception v0
    return-void
.end method

.method public static sameTypes(Z)V
    .locals 1
    if-eqz p0, :same_second

    :same_first_start
    invoke-static {}, Ltrycatch/TestSharedTypedHandlerRanges;->first()V
    :same_first_end
    .catch Ljava/lang/IllegalArgumentException; {:same_first_start .. :same_first_end} :same_handler
    goto :same_done

    :same_second
    :same_second_start
    invoke-static {}, Ltrycatch/TestSharedTypedHandlerRanges;->second()V
    :same_second_end
    .catch Ljava/lang/IllegalArgumentException; {:same_second_start .. :same_second_end} :same_handler

    :same_done
    return-void

    :same_handler
    move-exception v0
    return-void
.end method

.method public static sameMultiTypes(Z)V
    .locals 1
    if-eqz p0, :multi_second

    :multi_first_start
    invoke-static {}, Ltrycatch/TestSharedTypedHandlerRanges;->first()V
    :multi_first_end
    .catch Ljava/lang/IllegalArgumentException; {:multi_first_start .. :multi_first_end} :multi_handler
    .catch Ljava/lang/IllegalStateException; {:multi_first_start .. :multi_first_end} :multi_handler
    goto :multi_done

    :multi_second
    :multi_second_start
    invoke-static {}, Ltrycatch/TestSharedTypedHandlerRanges;->second()V
    :multi_second_end
    .catch Ljava/lang/IllegalArgumentException; {:multi_second_start .. :multi_second_end} :multi_handler
    .catch Ljava/lang/IllegalStateException; {:multi_second_start .. :multi_second_end} :multi_handler

    :multi_done
    return-void

    :multi_handler
    move-exception v0
    return-void
.end method

.method private static first()V
    .locals 0
    return-void
.end method

.method private static second()V
    .locals 0
    return-void
.end method
