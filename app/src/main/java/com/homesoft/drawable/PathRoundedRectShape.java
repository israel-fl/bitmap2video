package com.homesoft.drawable;

import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;

/*
 * Copyright (C) 2019 Homesoft, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class PathRoundedRectShape extends RectShape {
    final Path mPath = new Path();

    private boolean mClip;
    private float mCornerRadius;

    @Override
    public void draw(final Canvas canvas, final Paint paint) {
        if (mClip) {
            canvas.save();
            mPath.setFillType(Path.FillType.INVERSE_WINDING);
            canvas.clipPath(mPath);
            mPath.setFillType(Path.FillType.WINDING);
        }
        canvas.drawPath(mPath, paint);
        if (mClip) {
            canvas.restore();
        }
    }

    @Override
    protected void onResize(float width, float height) {
        super.onResize(width, height);
        mPath.reset();
        mPath.addRoundRect(rect(), mCornerRadius, mCornerRadius, Path.Direction.CW);
    }

    @Override
    public void getOutline(Outline outline) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            outline.setConvexPath(mPath);
        }
    }

    public void setCornerRadius(final float radius) {
        mCornerRadius = radius;
    }

    /**
     * Clip to the path.  This is only useful for drawing the shadow of a round rect
     * @param clip
     */
    public void setClip(boolean clip) {
        mClip = clip;
    }
}
