/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.init;

public class EarlyRiser implements Runnable {
    @Override
    public void run() {
        new ClientPlayerRiser().addTinkerers();
        new HandledScreenRiser().addTinkerers();
    }
}
