import { pgTable, text, timestamp, pgEnum } from "drizzle-orm/pg-core";
import { createInsertSchema } from "drizzle-zod";
import { z } from "zod/v4";

export const otpChannelEnum = pgEnum("otp_channel", ["email", "sms"]);
export const otpTypeEnum = pgEnum("otp_type", ["register", "reset_password"]);

export const otpsTable = pgTable("otps", {
  id: text("id").primaryKey().$defaultFn(() => crypto.randomUUID()),
  target: text("target").notNull(),
  channel: otpChannelEnum("channel").notNull(),
  type: otpTypeEnum("type").notNull(),
  code: text("code").notNull(),
  expiresAt: timestamp("expires_at", { withTimezone: true }).notNull(),
  usedAt: timestamp("used_at", { withTimezone: true }),
  createdAt: timestamp("created_at", { withTimezone: true }).notNull().defaultNow(),
});

export const insertOtpSchema = createInsertSchema(otpsTable).omit({
  id: true,
  createdAt: true,
  usedAt: true,
});

export type InsertOtp = z.infer<typeof insertOtpSchema>;
export type Otp = typeof otpsTable.$inferSelect;
