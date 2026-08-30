.class public Ltrycatch/TestCropImageSaveOutputExact;
.super Landroid/app/Activity;

.field private mContentResolver:Landroid/content/ContentResolver;
.field private mCrop:Lcom/android/camera/HighlightView;
.field private mHandler:Landroid/os/Handler;
.field private mImage:Lcom/android/camera/gallery/IImage;
.field private mOutputFormat:Landroid/graphics/Bitmap$CompressFormat;
.field private mOutputQuality:I
.field private mSaveUri:Landroid/net/Uri;
.field private mSetWallpaper:Z

.method private saveOutput(Landroid/graphics/Bitmap;)V
    .registers 18

    move-object/from16 v1, p0

    move-object/from16 v9, p1

    const-string v2, "Cannot open file: "

    .line 339
    iget-object v0, v1, Ltrycatch/TestCropImageSaveOutputExact;->mSaveUri:Landroid/net/Uri;

    const/4 v12, -0x1

    const-string v13, "CropImage"

    if-eqz v0, :cond_53

    const/4 v3, 0x0

    .line 342
    :try_start_e
    iget-object v4, v1, Ltrycatch/TestCropImageSaveOutputExact;->mContentResolver:Landroid/content/ContentResolver;

    invoke-virtual {v4, v0}, Landroid/content/ContentResolver;->openOutputStream(Landroid/net/Uri;)Ljava/io/OutputStream;

    move-result-object v3

    if-eqz v3, :cond_1d

    .line 344
    iget-object v0, v1, Ltrycatch/TestCropImageSaveOutputExact;->mOutputFormat:Landroid/graphics/Bitmap$CompressFormat;

    iget v4, v1, Ltrycatch/TestCropImageSaveOutputExact;->mOutputQuality:I

    invoke-virtual {v9, v0, v4, v3}, Landroid/graphics/Bitmap;->compress(Landroid/graphics/Bitmap$CompressFormat;ILjava/io/OutputStream;)Z
    :try_end_1d
    .catch Ljava/io/IOException; {:try_start_e .. :try_end_1d} :catch_23
    .catchall {:try_start_e .. :try_end_1d} :catchall_21

    .line 350
    :cond_1d
    :goto_1d
    invoke-static {v3}, Lcom/android/camera/Util;->closeSilently(Ljava/io/Closeable;)V

    goto :goto_36

    :catchall_21
    move-exception v0

    goto :goto_4f

    :catch_23
    move-exception v0

    .line 348
    :try_start_24
    new-instance v4, Ljava/lang/StringBuilder;

    invoke-direct {v4, v2}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    iget-object v2, v1, Ltrycatch/TestCropImageSaveOutputExact;->mSaveUri:Landroid/net/Uri;

    invoke-virtual {v4, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    invoke-virtual {v4}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-static {v13, v2, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    :try_end_35
    .catchall {:try_start_24 .. :try_end_35} :catchall_21

    goto :goto_1d

    .line 352
    :goto_36
    new-instance v0, Landroid/os/Bundle;

    invoke-direct {v0}, Landroid/os/Bundle;-><init>()V

    .line 353
    new-instance v2, Landroid/content/Intent;

    iget-object v3, v1, Ltrycatch/TestCropImageSaveOutputExact;->mSaveUri:Landroid/net/Uri;

    invoke-virtual {v3}, Landroid/net/Uri;->toString()Ljava/lang/String;

    move-result-object v3

    invoke-direct {v2, v3}, Landroid/content/Intent;-><init>(Ljava/lang/String;)V

    .line 354
    invoke-virtual {v2, v0}, Landroid/content/Intent;->putExtras(Landroid/os/Bundle;)Landroid/content/Intent;

    move-result-object v0

    .line 353
    invoke-virtual {v1, v12, v0}, Ltrycatch/TestCropImageSaveOutputExact;->setResult(ILandroid/content/Intent;)V

    goto/16 :goto_125

    .line 350
    :goto_4f
    invoke-static {v3}, Lcom/android/camera/Util;->closeSilently(Ljava/io/Closeable;)V

    throw v0

    .line 355
    :cond_53
    iget-boolean v0, v1, Ltrycatch/TestCropImageSaveOutputExact;->mSetWallpaper:Z

    const/4 v2, 0x0

    if-eqz v0, :cond_6f

    .line 357
    :try_start_58
    invoke-static {v1}, Landroid/app/WallpaperManager;->getInstance(Landroid/content/Context;)Landroid/app/WallpaperManager;

    move-result-object v0

    invoke-virtual {v0, v9}, Landroid/app/WallpaperManager;->setBitmap(Landroid/graphics/Bitmap;)V

    .line 358
    invoke-virtual {v1, v12}, Ltrycatch/TestCropImageSaveOutputExact;->setResult(I)V
    :try_end_62
    .catch Ljava/io/IOException; {:try_start_58 .. :try_end_62} :catch_64

    goto/16 :goto_125

    :catch_64
    move-exception v0

    .line 360
    const-string v3, "Failed to set wallpaper."

    invoke-static {v13, v3, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    .line 361
    invoke-virtual {v1, v2}, Ltrycatch/TestCropImageSaveOutputExact;->setResult(I)V

    goto/16 :goto_125

    .line 364
    :cond_6f
    new-instance v0, Landroid/os/Bundle;

    invoke-direct {v0}, Landroid/os/Bundle;-><init>()V

    .line 365
    iget-object v3, v1, Ltrycatch/TestCropImageSaveOutputExact;->mCrop:Lcom/android/camera/HighlightView;

    invoke-virtual {v3}, Lcom/android/camera/HighlightView;->getCropRect()Landroid/graphics/Rect;

    move-result-object v3

    invoke-virtual {v3}, Landroid/graphics/Rect;->toString()Ljava/lang/String;

    move-result-object v3

    const-string v4, "rect"

    invoke-virtual {v0, v4, v3}, Landroid/os/Bundle;->putString(Ljava/lang/String;Ljava/lang/String;)V

    .line 367
    new-instance v3, Ljava/io/File;

    iget-object v4, v1, Ltrycatch/TestCropImageSaveOutputExact;->mImage:Lcom/android/camera/gallery/IImage;

    invoke-interface {v4}, Lcom/android/camera/gallery/IImage;->getDataPath()Ljava/lang/String;

    move-result-object v4

    invoke-direct {v3, v4}, Ljava/io/File;-><init>(Ljava/lang/String;)V

    .line 368
    new-instance v4, Ljava/io/File;

    invoke-virtual {v3}, Ljava/io/File;->getParent()Ljava/lang/String;

    move-result-object v5

    invoke-direct {v4, v5}, Ljava/io/File;-><init>(Ljava/lang/String;)V

    .line 371
    invoke-virtual {v3}, Ljava/io/File;->getName()Ljava/lang/String;

    move-result-object v3

    .line 372
    const-string v5, "."

    invoke-virtual {v3, v5}, Ljava/lang/String;->lastIndexOf(Ljava/lang/String;)I

    move-result v5

    invoke-virtual {v3, v2, v5}, Ljava/lang/String;->substring(II)Ljava/lang/String;

    move-result-object v3

    :cond_a5
    const/4 v5, 0x1

    add-int/2addr v2, v5

    .line 378
    new-instance v6, Ljava/lang/StringBuilder;

    invoke-direct {v6}, Ljava/lang/StringBuilder;-><init>()V

    invoke-virtual {v4}, Ljava/io/File;->toString()Ljava/lang/String;

    move-result-object v7

    invoke-virtual {v6, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    const-string v7, "/"

    invoke-virtual {v6, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v6, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    const-string v7, "-"

    invoke-virtual {v6, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v6, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v8, ".jpg"

    invoke-virtual {v6, v8}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v6}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v6

    .line 380
    new-instance v10, Ljava/io/File;

    invoke-direct {v10, v6}, Ljava/io/File;-><init>(Ljava/lang/String;)V

    invoke-virtual {v10}, Ljava/io/File;->exists()Z

    move-result v6

    if-nez v6, :cond_a5

    .line 387
    :try_start_d7
    new-array v11, v5, [I

    .line 388
    iget-object v5, v1, Ltrycatch/TestCropImageSaveOutputExact;->mContentResolver:Landroid/content/ContentResolver;

    iget-object v6, v1, Ltrycatch/TestCropImageSaveOutputExact;->mImage:Lcom/android/camera/gallery/IImage;

    .line 390
    invoke-interface {v6}, Lcom/android/camera/gallery/IImage;->getTitle()Ljava/lang/String;

    move-result-object v6

    iget-object v10, v1, Ltrycatch/TestCropImageSaveOutputExact;->mImage:Lcom/android/camera/gallery/IImage;

    .line 391
    invoke-interface {v10}, Lcom/android/camera/gallery/IImage;->getDateTaken()J

    move-result-wide v14

    .line 394
    invoke-virtual {v4}, Ljava/io/File;->toString()Ljava/lang/String;

    move-result-object v4

    new-instance v10, Ljava/lang/StringBuilder;

    invoke-direct {v10}, Ljava/lang/StringBuilder;-><init>()V

    invoke-virtual {v10, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v10, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v10, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v10, v8}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v10}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v8

    const/4 v10, 0x0

    move-object v3, v6

    const/4 v6, 0x0

    move-object v7, v4

    move-object v2, v5

    move-wide v4, v14

    .line 388
    invoke-static/range {v2 .. v11}, Lcom/android/camera/ImageManager;->addImage(Landroid/content/ContentResolver;Ljava/lang/String;JLandroid/location/Location;Ljava/lang/String;Ljava/lang/String;Landroid/graphics/Bitmap;[B[I)Landroid/net/Uri;

    move-result-object v2

    .line 398
    new-instance v3, Landroid/content/Intent;

    invoke-direct {v3}, Landroid/content/Intent;-><init>()V

    .line 399
    invoke-virtual {v2}, Landroid/net/Uri;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-virtual {v3, v2}, Landroid/content/Intent;->setAction(Ljava/lang/String;)Landroid/content/Intent;

    move-result-object v2

    .line 400
    invoke-virtual {v2, v0}, Landroid/content/Intent;->putExtras(Landroid/os/Bundle;)Landroid/content/Intent;

    move-result-object v0

    .line 398
    invoke-virtual {v1, v12, v0}, Ltrycatch/TestCropImageSaveOutputExact;->setResult(ILandroid/content/Intent;)V
    :try_end_11e
    .catch Ljava/lang/Exception; {:try_start_d7 .. :try_end_11e} :catch_11f

    goto :goto_125

    :catch_11f
    move-exception v0

    .line 404
    const-string v2, "store image fail, continue anyway"

    invoke-static {v13, v2, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    .line 409
    :goto_125
    iget-object v0, v1, Ltrycatch/TestCropImageSaveOutputExact;->mHandler:Landroid/os/Handler;

    new-instance v2, Lcom/android/camera/CropImage$5;

    invoke-direct {v2, v1, v9}, Lcom/android/camera/CropImage$5;-><init>(Ltrycatch/TestCropImageSaveOutputExact;Landroid/graphics/Bitmap;)V

    invoke-virtual {v0, v2}, Landroid/os/Handler;->post(Ljava/lang/Runnable;)Z

    .line 416
    invoke-virtual {v1}, Ltrycatch/TestCropImageSaveOutputExact;->finish()V

    return-void
.end method
