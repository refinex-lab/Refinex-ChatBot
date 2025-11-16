"use client";

import Cropper, {Area} from "react-easy-crop";
import {useCallback, useState} from "react";
import {Dialog, DialogContent, DialogHeader, DialogTitle} from "@/components/ui/dialog";
import {Button} from "@/components/ui/button";
import {Slider} from "@/components/ui/slider";

export function AvatarCropper({
  open,
  src,
  onClose,
  onCropped,
}: {
  open: boolean;
  src: string;
  onClose: () => void;
  onCropped: (blob: Blob) => void;
}) {
  const [crop, setCrop] = useState<{ x: number; y: number }>({ x: 0, y: 0 });
  const [zoom, setZoom] = useState(1);
  const [croppedAreaPixels, setCroppedAreaPixels] = useState<Area | null>(null);

  const onCropComplete = useCallback((_croppedArea: Area, croppedPixels: Area) => {
    setCroppedAreaPixels(croppedPixels);
  }, []);

  const doCrop = useCallback(async () => {
    if (!croppedAreaPixels) return;
    const img = await loadImage(src);
    const canvas = document.createElement("canvas");
    const size = 512;
    canvas.width = size;
    canvas.height = size;
    const ctx = canvas.getContext("2d")!;
    const { x, y, width, height } = croppedAreaPixels;
    ctx.drawImage(img, x, y, width, height, 0, 0, size, size);
    const blob: Blob = await new Promise((resolve) => canvas.toBlob((b) => resolve(b!), "image/jpeg", 0.92));
    onCropped(blob);
    onClose();
  }, [croppedAreaPixels, onClose, onCropped, src]);

  return (
    <Dialog open={open} onOpenChange={(o) => !o && onClose()}>
      {/* 给裁剪对话框更充裕的内边距与尺寸，避免四周“贴边” */}
      <DialogContent className="max-w-2xl p-4 sm:p-6">
        <DialogHeader>
          <DialogTitle>裁剪头像</DialogTitle>
        </DialogHeader>
        {/* 画布区域：加大高度、留出圆角与间距 */}
        <div className="relative h-[420px] w-full overflow-hidden rounded-xl bg-muted sm:h-[460px]">
          <Cropper
            image={src}
            crop={crop}
            zoom={zoom}
            aspect={1}
            cropShape="round"
            showGrid={false}
            onCropChange={setCrop}
            onCropComplete={onCropComplete}
            onZoomChange={setZoom}
          />
        </div>
        {/* 控制条与按钮：留足上下间距 */}
        <div className="mt-4 flex items-center gap-4">
          <span className="w-16 shrink-0 text-sm text-muted-foreground">缩放</span>
          <Slider min={1} max={3} step={0.1} value={[zoom]} onValueChange={(v) => setZoom(v[0] ?? 1)} />
        </div>
        <div className="mt-4 flex justify-end gap-2">
          <Button variant="outline" onClick={onClose}>
            取消
          </Button>
          <Button onClick={doCrop}>应用</Button>
        </div>
      </DialogContent>
    </Dialog>
  );
}

function loadImage(src: string): Promise<HTMLImageElement> {
  return new Promise((resolve, reject) => {
    const img = new Image();
    img.onload = () => resolve(img);
    img.onerror = reject;
    img.src = src;
  });
}
