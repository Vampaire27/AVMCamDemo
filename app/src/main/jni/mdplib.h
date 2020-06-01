/* Copyright Statement:
 *
 * This software/firmware and related documentation ("AutoChips Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to AutoChips Inc. and/or its licensors. Without
 * the prior written permission of AutoChips inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of AutoChips Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 *
 * AutoChips Inc. (C) 2019. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("AUTOCHIPS SOFTWARE")
 * RECEIVED FROM AUTOCHIPS AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. AUTOCHIPS EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES AUTOCHIPS PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE AUTOCHIPS SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN AUTOCHIPS
 * SOFTWARE. AUTOCHIPS SHALL ALSO NOT BE RESPONSIBLE FOR ANY AUTOCHIPS SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND AUTOCHIPS'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE AUTOCHIPS SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT AUTOCHIPS'S OPTION, TO REVISE OR REPLACE THE
 * AUTOCHIPS SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO AUTOCHIPS FOR SUCH AUTOCHIPS SOFTWARE AT ISSUE.
 */


#ifndef __MDPLIB_H__
#define __MDPLIB_H__

#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

enum {
    MDP_PIXEL_FORMAT_NONE = 0,
    MDP_PIXEL_FORMAT_YUYV,
    MDP_PIXEL_FORMAT_YVYU,
    MDP_PIXEL_FORMAT_UYVY,
    MDP_PIXEL_FORMAT_VYUY,
    MDP_PIXEL_FORMAT_I420,
    MDP_PIXEL_FORMAT_YV12,
    MDP_PIXEL_FORMAT_NV21,
    MDP_PIXEL_FORMAT_NV12,
};

enum {
    MDP_FLIP_H = (1 << 0),
    MDP_FLIP_V = (1 << 1),
};

typedef void* mdp_handle;

mdp_handle mdplib_create (void);
int mdplib_set_src_buffer (mdp_handle h, void* plane_ptr[], uint32_t plane_siz[], uint32_t plane_num);
int mdplib_set_src_buffer2 (mdp_handle h, uint32_t fd, uint32_t plane_siz[], uint32_t plane_num);
int mdplib_set_dst_buffer (mdp_handle h, void* plane_ptr[], uint32_t plane_siz[], uint32_t plane_num);
int mdplib_set_dst_buffer2 (mdp_handle h, uint32_t fd, uint32_t plane_siz[], uint32_t plane_num);
int mdplib_set_src_config (mdp_handle h, int32_t width, int32_t height, int32_t ypitch, int32_t uvpitch, int32_t format, int32_t crop[4]);
int mdplib_set_dst_config (mdp_handle h, int32_t width, int32_t height, int32_t ypitch, int32_t uvpitch, int32_t format);
int mdplib_set_flip (mdp_handle h, int32_t flip);
int mdplib_invalidate (mdp_handle h);
int mdplib_destroy (mdp_handle h);

//for dlopen
typedef struct {
    mdp_handle (*create) (void);
    int (*set_src_buffer) (mdp_handle, void*[], uint32_t[], uint32_t);
    int (*set_src_buffer2) (mdp_handle, uint32_t, uint32_t[], uint32_t);
    int (*set_dst_buffer) (mdp_handle, void*[], uint32_t[], uint32_t);
    int (*set_dst_buffer2) (mdp_handle, uint32_t, uint32_t[], uint32_t);
    int (*set_src_config) (mdp_handle, int32_t, int32_t, int32_t, int32_t, int32_t, int32_t[]);
    int (*set_dst_config) (mdp_handle, int32_t, int32_t, int32_t, int32_t, int32_t);
    int (*set_flip) (mdp_handle, int32_t);
    int (*invalidate) (mdp_handle);
    int (*destroy) (mdp_handle);
} mdplib_ops;
const mdplib_ops* mdplib_get_ops (void);

#ifdef __cplusplus
}
#endif
#endif
