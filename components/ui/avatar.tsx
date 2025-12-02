"use client"

import * as React from "react"
import * as AvatarPrimitive from "@radix-ui/react-avatar"

import { cn } from "@/lib/utils"

/**
 * Wraps Radix's Avatar Root with default avatar styling and forwards all props.
 *
 * @param className - Additional class name(s) to append to the component's default avatar classes
 * @returns A Radix Avatar Root element with the default avatar classes and any provided `className` merged, forwarding all other props
 */
function Avatar({
  className,
  ...props
}: React.ComponentProps<typeof AvatarPrimitive.Root>) {
  return (
    <AvatarPrimitive.Root
      data-slot="avatar"
      className={cn(
        "relative flex size-8 shrink-0 overflow-hidden rounded-full",
        className
      )}
      {...props}
    />
  )
}

/**
 * Render the image portion of an avatar with default sizing and aspect classes.
 *
 * Accepts the same props as `AvatarPrimitive.Image`; any `className` provided is appended to the component's default classes.
 *
 * @param className - Additional class names appended to the default `"aspect-square size-full"` classes
 * @returns The rendered avatar image element
 */
function AvatarImage({
  className,
  ...props
}: React.ComponentProps<typeof AvatarPrimitive.Image>) {
  return (
    <AvatarPrimitive.Image
      data-slot="avatar-image"
      className={cn("aspect-square size-full", className)}
      {...props}
    />
  )
}

/**
 * Renders the fallback content for an avatar when the image is unavailable.
 *
 * @returns The Avatar fallback element with composed classes and any forwarded props
 */
function AvatarFallback({
  className,
  ...props
}: React.ComponentProps<typeof AvatarPrimitive.Fallback>) {
  return (
    <AvatarPrimitive.Fallback
      data-slot="avatar-fallback"
      className={cn(
        "bg-muted flex size-full items-center justify-center rounded-full",
        className
      )}
      {...props}
    />
  )
}

export { Avatar, AvatarImage, AvatarFallback }