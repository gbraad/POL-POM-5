/*
 * Copyright (C) 2015-2017 PÂRIS Quentin
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.phoenicis.tools.system;

import org.phoenicis.tools.system.terminal.AutomaticTerminalOpener;
import org.phoenicis.tools.system.terminal.TerminalOpener;
import org.phoenicis.tools.system.terminal.TerminalOpenerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SystemConfiguration {
    @Bean
    public TerminalOpener terminalOpener() {
        return new AutomaticTerminalOpener(terminalOpenerFactory(), operatingSystemFetcher());
    }

    @Bean
    public TerminalOpenerFactory terminalOpenerFactory() {
        return new TerminalOpenerFactory();
    }

    @Bean
    public OperatingSystemFetcher operatingSystemFetcher() {
        return new OperatingSystemFetcher();
    }
}
