/*
 * Copyright (C) 2017 Haruki Hasegawa
 * Copyright (C) 2008 The Android Open Source Project
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
package com.h6ah4i.android.widget.numberpickercompat;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewParentCompat;
import androidx.core.view.accessibility.AccessibilityEventCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeProviderCompat;
import androidx.core.view.accessibility.AccessibilityRecordCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.EditText;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class for managing virtual view tree rooted at this picker.
 */
class AccessibilityNodeProviderImpl extends AccessibilityNodeProviderCompat {
    static final int UNDEFINED = Integer.MIN_VALUE;
    static final int VIRTUAL_VIEW_ID_INCREMENT = 1;
    static final int VIRTUAL_VIEW_ID_INPUT = 2;
    static final int VIRTUAL_VIEW_ID_DECREMENT = 3;

    private final Rect mTempRect = new Rect();
    private final int[] mTempArray = new int[2];
    private int mAccessibilityFocusedView = UNDEFINED;
    private NumberPicker mNumberPicker;

    AccessibilityNodeProviderImpl(NumberPicker numberPicker) {
        mNumberPicker = numberPicker;
    }

    @Override
    public AccessibilityNodeInfoCompat createAccessibilityNodeInfo(int virtualViewId) {
        final int mTop = mNumberPicker.getTop();
        final int mBottom = mNumberPicker.getBottom();
        final int mLeft = mNumberPicker.getLeft();
        final int mRight = mNumberPicker.getRight();
        final int mScrollX = mNumberPicker.getScrollX();
        final int mScrollY = mNumberPicker.getScrollY();
        final int mTopSelectionDividerTop = mNumberPicker.mTopSelectionDividerTop;
        final int mBottomSelectionDividerBottom = mNumberPicker.mBottomSelectionDividerBottom;
        final int mSelectionDividerHeight = mNumberPicker.mSelectionDividerHeight;

        switch (virtualViewId) {
            case View.NO_ID:
                return createAccessibilityNodeInfoForNumberPicker(mScrollX, mScrollY,
                        mScrollX + (mRight - mLeft), mScrollY + (mBottom - mTop));
            case VIRTUAL_VIEW_ID_DECREMENT:
                return createAccessibilityNodeInfoForVirtualButton(VIRTUAL_VIEW_ID_DECREMENT,
                        getVirtualDecrementButtonText(), mScrollX, mScrollY,
                        mScrollX + (mRight - mLeft),
                        mTopSelectionDividerTop + mSelectionDividerHeight);
            case VIRTUAL_VIEW_ID_INPUT:
                return createAccessibilityNodeInfoForInputText(mScrollX,
                        mTopSelectionDividerTop + mSelectionDividerHeight,
                        mScrollX + (mRight - mLeft),
                        mBottomSelectionDividerBottom - mSelectionDividerHeight);
            case VIRTUAL_VIEW_ID_INCREMENT:
                return createAccessibilityNodeInfoForVirtualButton(VIRTUAL_VIEW_ID_INCREMENT,
                        getVirtualIncrementButtonText(), mScrollX,
                        mBottomSelectionDividerBottom - mSelectionDividerHeight,
                        mScrollX + (mRight - mLeft), mScrollY + (mBottom - mTop));
        }
        return super.createAccessibilityNodeInfo(virtualViewId);
    }

    @Override
    public List<AccessibilityNodeInfoCompat> findAccessibilityNodeInfosByText(String searched,
                                                                              int virtualViewId) {
        if (TextUtils.isEmpty(searched)) {
            return Collections.emptyList();
        }
        String searchedLowerCase = searched.toLowerCase();
        List<AccessibilityNodeInfoCompat> result = new ArrayList<>();
        switch (virtualViewId) {
            case View.NO_ID: {
                findAccessibilityNodeInfosByTextInChild(searchedLowerCase,
                        VIRTUAL_VIEW_ID_DECREMENT, result);
                findAccessibilityNodeInfosByTextInChild(searchedLowerCase,
                        VIRTUAL_VIEW_ID_INPUT, result);
                findAccessibilityNodeInfosByTextInChild(searchedLowerCase,
                        VIRTUAL_VIEW_ID_INCREMENT, result);
                return result;
            }
            case VIRTUAL_VIEW_ID_DECREMENT:
            case VIRTUAL_VIEW_ID_INCREMENT:
            case VIRTUAL_VIEW_ID_INPUT: {
                findAccessibilityNodeInfosByTextInChild(searchedLowerCase, virtualViewId,
                        result);
                return result;
            }
        }
        return super.findAccessibilityNodeInfosByText(searched, virtualViewId);
    }

    @Override
    public boolean performAction(int virtualViewId, int action, Bundle arguments) {
        switch (virtualViewId) {
            case View.NO_ID: {
                switch (action) {
                    case AccessibilityNodeInfoCompat.ACTION_ACCESSIBILITY_FOCUS: {
                        if (mAccessibilityFocusedView != virtualViewId) {
                            mAccessibilityFocusedView = virtualViewId;
                            requestAccessibilityFocus();
                            return true;
                        }
                    }
                    return false;
                    case AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS: {
                        if (mAccessibilityFocusedView == virtualViewId) {
                            mAccessibilityFocusedView = UNDEFINED;
                            clearAccessibilityFocus();
                            return true;
                        }
                        return false;
                    }
                    case AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD: {
                        if (isEnabled()
                                && (getWrapSelectorWheel() || getValue() < getMaxValue())) {
                            mNumberPicker.changeValueByOne(true);
                            return true;
                        }
                    }
                    return false;
                    case AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD: {
                        if (isEnabled()
                                && (getWrapSelectorWheel() || getValue() > getMinValue())) {
                            mNumberPicker.changeValueByOne(false);
                            return true;
                        }
                    }
                    return false;
                }
            }
            break;
            case VIRTUAL_VIEW_ID_INPUT: {
                switch (action) {
                    case AccessibilityNodeInfoCompat.ACTION_FOCUS: {
                        if (isEnabled() && !getInputText().isFocused()) {
                            return getInputText().requestFocus();
                        }
                    }
                    break;
                    case AccessibilityNodeInfoCompat.ACTION_CLEAR_FOCUS: {
                        if (isEnabled() && getInputText().isFocused()) {
                            getInputText().clearFocus();
                            return true;
                        }
                        return false;
                    }
                    case AccessibilityNodeInfoCompat.ACTION_CLICK: {
                        if (isEnabled()) {
                            mNumberPicker.performClick();
                            return true;
                        }
                        return false;
                    }
                    case AccessibilityNodeInfoCompat.ACTION_LONG_CLICK: {
                        if (isEnabled()) {
                            mNumberPicker.performLongClick();
                            return true;
                        }
                        return false;
                    }
                    case AccessibilityNodeInfoCompat.ACTION_ACCESSIBILITY_FOCUS: {
                        if (mAccessibilityFocusedView != virtualViewId) {
                            mAccessibilityFocusedView = virtualViewId;
                            sendAccessibilityEventForVirtualView(virtualViewId,
                                    AccessibilityEventCompat.TYPE_VIEW_ACCESSIBILITY_FOCUSED);
                            getInputText().invalidate();
                            return true;
                        }
                    }
                    return false;
                    case AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS: {
                        if (mAccessibilityFocusedView == virtualViewId) {
                            mAccessibilityFocusedView = UNDEFINED;
                            sendAccessibilityEventForVirtualView(virtualViewId,
                                    AccessibilityEventCompat.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED);
                            getInputText().invalidate();
                            return true;
                        }
                    }
                    return false;
                    default: {
                        ViewCompat.performAccessibilityAction(getInputText(), action, arguments);
                    }
                }
            }
            return false;
            case VIRTUAL_VIEW_ID_INCREMENT: {
                switch (action) {
                    case AccessibilityNodeInfoCompat.ACTION_CLICK: {
                        if (isEnabled()) {
                            mNumberPicker.changeValueByOne(true);
                            sendAccessibilityEventForVirtualView(virtualViewId,
                                    AccessibilityEvent.TYPE_VIEW_CLICKED);
                            return true;
                        }
                    }
                    return false;
                    case AccessibilityNodeInfoCompat.ACTION_ACCESSIBILITY_FOCUS: {
                        if (mAccessibilityFocusedView != virtualViewId) {
                            mAccessibilityFocusedView = virtualViewId;
                            sendAccessibilityEventForVirtualView(virtualViewId,
                                    AccessibilityEventCompat.TYPE_VIEW_ACCESSIBILITY_FOCUSED);
                            mNumberPicker.invalidate(
                                    0, mNumberPicker.mBottomSelectionDividerBottom,
                                    mNumberPicker.getRight(), mNumberPicker.getBottom());
                            return true;
                        }
                    }
                    return false;
                    case AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS: {
                        if (mAccessibilityFocusedView == virtualViewId) {
                            mAccessibilityFocusedView = UNDEFINED;
                            sendAccessibilityEventForVirtualView(virtualViewId,
                                    AccessibilityEventCompat.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED);
                            mNumberPicker.invalidate(
                                    0, mNumberPicker.mBottomSelectionDividerBottom,
                                    mNumberPicker.getRight(), mNumberPicker.getBottom());
                            return true;
                        }
                    }
                    return false;
                }
            }
            return false;
            case VIRTUAL_VIEW_ID_DECREMENT: {
                switch (action) {
                    case AccessibilityNodeInfoCompat.ACTION_CLICK: {
                        if (isEnabled()) {
                            final boolean increment = (virtualViewId == VIRTUAL_VIEW_ID_INCREMENT);
                            mNumberPicker.changeValueByOne(increment);
                            sendAccessibilityEventForVirtualView(virtualViewId,
                                    AccessibilityEvent.TYPE_VIEW_CLICKED);
                            return true;
                        }
                    }
                    return false;
                    case AccessibilityNodeInfoCompat.ACTION_ACCESSIBILITY_FOCUS: {
                        if (mAccessibilityFocusedView != virtualViewId) {
                            mAccessibilityFocusedView = virtualViewId;
                            sendAccessibilityEventForVirtualView(virtualViewId,
                                    AccessibilityEventCompat.TYPE_VIEW_ACCESSIBILITY_FOCUSED);
                            mNumberPicker.invalidate(0, 0, mNumberPicker.getRight(), mNumberPicker.mTopSelectionDividerTop);
                            return true;
                        }
                    }
                    return false;
                    case AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS: {
                        if (mAccessibilityFocusedView == virtualViewId) {
                            mAccessibilityFocusedView = UNDEFINED;
                            sendAccessibilityEventForVirtualView(virtualViewId,
                                    AccessibilityEventCompat.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED);
                            mNumberPicker.invalidate(0, 0, mNumberPicker.getRight(), mNumberPicker.mTopSelectionDividerTop);
                            return true;
                        }
                    }
                    return false;
                }
            }
            return false;
        }
        return super.performAction(virtualViewId, action, arguments);
    }

    public void sendAccessibilityEventForVirtualView(int virtualViewId, int eventType) {
        switch (virtualViewId) {
            case VIRTUAL_VIEW_ID_DECREMENT: {
                if (hasVirtualDecrementButton()) {
                    sendAccessibilityEventForVirtualButton(virtualViewId, eventType,
                            getVirtualDecrementButtonText());
                }
            }
            break;
            case VIRTUAL_VIEW_ID_INPUT: {
                sendAccessibilityEventForVirtualText(eventType);
            }
            break;
            case VIRTUAL_VIEW_ID_INCREMENT: {
                if (hasVirtualIncrementButton()) {
                    sendAccessibilityEventForVirtualButton(virtualViewId, eventType,
                            getVirtualIncrementButtonText());
                }
            }
            break;
        }
    }

    private void sendAccessibilityEventForVirtualText(int eventType) {
        AccessibilityManager accessibilityManager = (AccessibilityManager) getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (accessibilityManager.isEnabled()) {
            AccessibilityEvent event = AccessibilityEvent.obtain(eventType);
            AccessibilityRecordCompat record = AccessibilityEventCompat.asRecord(event);
            ViewCompat.onInitializeAccessibilityEvent(getInputText(), event);
            ViewCompat.onPopulateAccessibilityEvent(getInputText(), event);
            record.setSource(mNumberPicker, VIRTUAL_VIEW_ID_INPUT);

            ViewParentCompat.requestSendAccessibilityEvent(mNumberPicker, mNumberPicker, event);
        }
    }

    private void sendAccessibilityEventForVirtualButton(int virtualViewId, int eventType,
                                                        String text) {
        AccessibilityManager accessibilityManager = (AccessibilityManager) getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (accessibilityManager.isEnabled()) {
            AccessibilityEvent event = AccessibilityEvent.obtain(eventType);
            AccessibilityRecordCompat record = AccessibilityEventCompat.asRecord(event);
            event.setPackageName(getContext().getPackageName());
            record.setClassName(Button.class.getName());
            record.getText().add(text);
            record.setEnabled(isEnabled());
            record.setSource(mNumberPicker, virtualViewId);

            ViewParentCompat.requestSendAccessibilityEvent(mNumberPicker, mNumberPicker, event);
        }
    }

    private void findAccessibilityNodeInfosByTextInChild(String searchedLowerCase,
                                                         int virtualViewId, List<AccessibilityNodeInfoCompat> outResult) {
        switch (virtualViewId) {
            case VIRTUAL_VIEW_ID_DECREMENT: {
                String text = getVirtualDecrementButtonText();
                if (!TextUtils.isEmpty(text)
                        && text.toString().toLowerCase().contains(searchedLowerCase)) {
                    outResult.add(createAccessibilityNodeInfo(VIRTUAL_VIEW_ID_DECREMENT));
                }
            }
            return;
            case VIRTUAL_VIEW_ID_INPUT: {
                CharSequence text = getInputText().getText();
                if (!TextUtils.isEmpty(text) &&
                        text.toString().toLowerCase().contains(searchedLowerCase)) {
                    outResult.add(createAccessibilityNodeInfo(VIRTUAL_VIEW_ID_INPUT));
                    return;
                }
                CharSequence contentDesc = getInputText().getText();
                if (!TextUtils.isEmpty(contentDesc) &&
                        contentDesc.toString().toLowerCase().contains(searchedLowerCase)) {
                    outResult.add(createAccessibilityNodeInfo(VIRTUAL_VIEW_ID_INPUT));
                    return;
                }
            }
            break;
            case VIRTUAL_VIEW_ID_INCREMENT: {
                String text = getVirtualIncrementButtonText();
                if (!TextUtils.isEmpty(text)
                        && text.toString().toLowerCase().contains(searchedLowerCase)) {
                    outResult.add(createAccessibilityNodeInfo(VIRTUAL_VIEW_ID_INCREMENT));
                }
            }
            return;
        }
    }

    private AccessibilityNodeInfoCompat createAccessibilityNodeInfoForInputText(
            int left, int top, int right, int bottom) {
        AccessibilityNodeInfoCompat info = AccessibilityNodeInfoCompat.obtain(getInputText());
        info.setSource(mNumberPicker, VIRTUAL_VIEW_ID_INPUT);
        if (mAccessibilityFocusedView != VIRTUAL_VIEW_ID_INPUT) {
            info.addAction(AccessibilityNodeInfoCompat.ACTION_ACCESSIBILITY_FOCUS);
        }
        if (mAccessibilityFocusedView == VIRTUAL_VIEW_ID_INPUT) {
            info.addAction(AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
        }
        Rect boundsInParent = mTempRect;
        boundsInParent.set(left, top, right, bottom);
        info.setVisibleToUser(isVisibleToUser(boundsInParent));
        info.setBoundsInParent(boundsInParent);
        Rect boundsInScreen = boundsInParent;
        int[] locationOnScreen = mTempArray;
        mNumberPicker.getLocationOnScreen(locationOnScreen);
        boundsInScreen.offset(locationOnScreen[0], locationOnScreen[1]);
        info.setBoundsInScreen(boundsInScreen);
        return info;
    }

    private AccessibilityNodeInfoCompat createAccessibilityNodeInfoForVirtualButton(int virtualViewId,
                                                                                    String text, int left, int top, int right, int bottom) {
        AccessibilityNodeInfoCompat info = AccessibilityNodeInfoCompat.obtain();
        info.setClassName(Button.class.getName());
        info.setPackageName(getContext().getPackageName());
        info.setSource(mNumberPicker, virtualViewId);
        info.setParent(mNumberPicker);
        info.setText(text);
        info.setClickable(true);
        info.setLongClickable(true);
        info.setEnabled(isEnabled());
        Rect boundsInParent = mTempRect;
        boundsInParent.set(left, top, right, bottom);
        info.setVisibleToUser(isVisibleToUser(boundsInParent));
        info.setBoundsInParent(boundsInParent);
        Rect boundsInScreen = boundsInParent;
        int[] locationOnScreen = mTempArray;
        mNumberPicker.getLocationOnScreen(locationOnScreen);
        boundsInScreen.offset(locationOnScreen[0], locationOnScreen[1]);
        info.setBoundsInScreen(boundsInScreen);
        if (mAccessibilityFocusedView != virtualViewId) {
            info.addAction(AccessibilityNodeInfoCompat.ACTION_ACCESSIBILITY_FOCUS);
        }
        if (mAccessibilityFocusedView == virtualViewId) {
            info.addAction(AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
        }
        if (isEnabled()) {
            info.addAction(AccessibilityNodeInfoCompat.ACTION_CLICK);
        }
        return info;
    }

    private AccessibilityNodeInfoCompat createAccessibilityNodeInfoForNumberPicker(int left, int top,
                                                                                   int right, int bottom) {
        AccessibilityNodeInfoCompat info = AccessibilityNodeInfoCompat.obtain();
        info.setClassName(NumberPicker.class.getName());
        info.setPackageName(getContext().getPackageName());
        info.setSource(mNumberPicker);
        if (hasVirtualDecrementButton()) {
            info.addChild(mNumberPicker, VIRTUAL_VIEW_ID_DECREMENT);
        }
        info.addChild(mNumberPicker, VIRTUAL_VIEW_ID_INPUT);
        if (hasVirtualIncrementButton()) {
            info.addChild(mNumberPicker, VIRTUAL_VIEW_ID_INCREMENT);
        }
        info.setParent((View) ViewCompat.getParentForAccessibility(mNumberPicker));
        info.setEnabled(isEnabled());
        info.setScrollable(true);
        final float applicationScale = getApplicationScale();
        Rect boundsInParent = mTempRect;
        boundsInParent.set(left, top, right, bottom);
        scale(boundsInParent, applicationScale);
        info.setBoundsInParent(boundsInParent);
        info.setVisibleToUser(isVisibleToUser());
        Rect boundsInScreen = boundsInParent;
        int[] locationOnScreen = mTempArray;
        mNumberPicker.getLocationOnScreen(locationOnScreen);
        boundsInScreen.offset(locationOnScreen[0], locationOnScreen[1]);
        scale(boundsInScreen, applicationScale);
        info.setBoundsInScreen(boundsInScreen);
        if (mAccessibilityFocusedView != View.NO_ID) {
            info.addAction(AccessibilityNodeInfoCompat.ACTION_ACCESSIBILITY_FOCUS);
        }
        if (mAccessibilityFocusedView == View.NO_ID) {
            info.addAction(AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
        }
        if (isEnabled()) {
            if (getWrapSelectorWheel() || getValue() < getMaxValue()) {
                info.addAction(AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD);
            }
            if (getWrapSelectorWheel() || getValue() > getMinValue()) {
                info.addAction(AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD);
            }
        }
        return info;
    }

    private boolean hasVirtualDecrementButton() {
        return getWrapSelectorWheel() || getValue() > getMinValue();
    }

    private boolean hasVirtualIncrementButton() {
        return getWrapSelectorWheel() || getValue() < getMaxValue();
    }

    private String getVirtualDecrementButtonText() {
        int value = getValue() - 1;
        if (getWrapSelectorWheel()) {
            value = mNumberPicker.getWrappedSelectorIndex(value);
        }
        if (value >= getMinValue()) {
            return (mNumberPicker.mDisplayedValues == null) ? mNumberPicker.formatNumber(value)
                    : mNumberPicker.mDisplayedValues[value - getMinValue()];
        }
        return null;
    }

    private String getVirtualIncrementButtonText() {
        int value = getValue() + 1;
        if (getWrapSelectorWheel()) {
            value = mNumberPicker.getWrappedSelectorIndex(value);
        }
        if (value <= getMaxValue()) {
            return (mNumberPicker.mDisplayedValues == null) ? mNumberPicker.formatNumber(value)
                    : mNumberPicker.mDisplayedValues[value - getMinValue()];
        }
        return null;
    }

    private Context getContext() {
        return mNumberPicker.getContext();
    }

    private boolean isEnabled() {
        return mNumberPicker.isEnabled();
    }

    private int getValue() {
        return mNumberPicker.getValue();
    }

    private int getMinValue() {
        return mNumberPicker.getMinValue();
    }

    private int getMaxValue() {
        return mNumberPicker.getMaxValue();
    }

    private boolean getWrapSelectorWheel() {
        return mNumberPicker.getWrapSelectorWheel();
    }

    private EditText getInputText() {
        return mNumberPicker.mInputText;
    }


    private Method mRequestAccessibilityFocus;
    private Method mClearAccessibilityFocus;
    private Method mIsVisibleToUser;

    private void requestAccessibilityFocus() {
        try {
            if (mRequestAccessibilityFocus == null) {
                mRequestAccessibilityFocus = View.class.getMethod("requestAccessibilityFocus");
            }
            mRequestAccessibilityFocus.invoke(mNumberPicker);
        } catch (NoSuchMethodException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
    }

    private void clearAccessibilityFocus() {
        try {
            if (mClearAccessibilityFocus == null) {
                mClearAccessibilityFocus = View.class.getMethod("clearAccessibilityFocus");
            }
            mClearAccessibilityFocus.invoke(mNumberPicker);
        } catch (NoSuchMethodException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
    }

    private float getApplicationScale() {
        return 1.0f;
    }

    private boolean isVisibleToUser() {
        return isVisibleToUser(null);
    }

    private boolean isVisibleToUser(Rect rect) {
        try {
            if (mIsVisibleToUser == null) {
                mIsVisibleToUser = View.class.getDeclaredMethod("isVisibleToUser", Rect.class);
                mIsVisibleToUser.setAccessible(true);
            }
            return (Boolean) mIsVisibleToUser.invoke(mNumberPicker, rect);
        } catch (NoSuchMethodException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }

        return true;
    }

    static void scale(Rect rect, float scale) {
        if (scale != 1.0f) {
            rect.left = (int) (rect.left * scale + 0.5f);
            rect.top = (int) (rect.top * scale + 0.5f);
            rect.right = (int) (rect.right * scale + 0.5f);
            rect.bottom = (int) (rect.bottom * scale + 0.5f);
        }
    }
}