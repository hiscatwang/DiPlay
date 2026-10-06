package com.shilapi.xcertplay.network

import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress

/**
 * Prefer a manual hotspot's IPv4 path when both families exist. Harman Android 8.1
 * reports a scoped IPv6 address even when CarPlay discovery never reaches a peer
 * on that path. The selected address also drives Bonjour, probing and AirPlay.
 * Keep scoped link-local IPv6 as a fallback for hotspots without usable IPv4.
 */
internal fun wirelessHostAddress(addresses: List<InetAddress>, interfaceIndex: Int): InetAddress? {
    addresses.firstOrNull {
        it is Inet4Address && !it.isLoopbackAddress && !it.isLinkLocalAddress &&
            !it.isAnyLocalAddress && !it.isMulticastAddress
    }?.let { return it }
    if (interfaceIndex > 0) {
        addresses.filterIsInstance<Inet6Address>().firstOrNull { it.isLinkLocalAddress }?.let {
            return Inet6Address.getByAddress(null, it.address, interfaceIndex)
        }
    }
    return null
}

/** Station LAN discovery must cover IPv4 multicast as well as scoped link-local IPv6. */
internal fun existingWifiHostAddresses(addresses: List<InetAddress>, interfaceIndex: Int): List<InetAddress> {
    val ipv4 = addresses.firstOrNull {
        it is Inet4Address && !it.isLoopbackAddress && !it.isLinkLocalAddress &&
            !it.isAnyLocalAddress && !it.isMulticastAddress
    }
    val ipv6 = if (interfaceIndex > 0) {
        addresses.filterIsInstance<Inet6Address>().firstOrNull { it.isLinkLocalAddress }
            ?.let { Inet6Address.getByAddress(null, it.address, interfaceIndex) }
    } else null
    return listOfNotNull(ipv4, ipv6)
}
