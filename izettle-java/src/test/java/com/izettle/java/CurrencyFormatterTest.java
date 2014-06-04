package com.izettle.java;

import static com.izettle.java.CurrencyFormatter.format;
import static com.izettle.java.CurrencyFormatter.parse;
import static com.izettle.java.CurrencyId.EUR;
import static com.izettle.java.CurrencyId.IQD;
import static com.izettle.java.CurrencyId.NZD;
import static com.izettle.java.CurrencyId.SEK;
import static com.izettle.java.CurrencyId.USD;
import static org.junit.Assert.assertEquals;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import org.junit.Test;

public class CurrencyFormatterTest {

	private static final Locale LOCALE_SE = new Locale(LanguageId.sv.name(), CountryId.SE.name());

	@Test
	public void itShouldParseFullformatCorrectly() throws Exception {
		assertEquals(10000L, parse(SEK, LOCALE_SE, "100,00 kr"));
		assertEquals(10000L, parse(SEK, LOCALE_SE, "100,00 kr"));
		assertEquals(0L, parse(SEK, LOCALE_SE, "0,00 kr"));
		assertEquals(-100L, parse(SEK, LOCALE_SE, "-1,00 kr"));
		assertEquals(-10000000L, parse(SEK, LOCALE_SE, "-100 000,00 kr"));
		assertEquals(-10000025L, parse(SEK, LOCALE_SE, "-100 000,25 kr"));
		assertEquals(10000025L, parse(SEK, LOCALE_SE, "100 000,25 kr"));
		assertEquals(25L, parse(SEK, LOCALE_SE, "0,25 kr"));
		assertEquals(-25L, parse(SEK, LOCALE_SE, "-0,25 kr"));
		assertEquals(10000025L, parse(EUR, LOCALE_SE, "100 000,25 €"));
		assertEquals(0L, parse(EUR, Locale.UK, "€0.00"));
		assertEquals(0L, parse(EUR, Locale.US, "EUR0.00"));
		assertEquals(-100L, parse(EUR, Locale.US, "(EUR1.00)"));
		assertEquals(-10000000L, parse(EUR, Locale.UK, "-€100,000.00"));
		assertEquals(-10000025L, parse(EUR, Locale.UK, "-€100,000.25"));
		assertEquals(25L, parse(EUR, Locale.US, "EUR0.25"));
		assertEquals(-25L, parse(EUR, Locale.US, "(EUR0.25)"));
	}

	private static final Set<CurrencyId> VALID_CURRENCIES = EnumSet.complementOf(EnumSet.of(
			CurrencyId.JEP,
			CurrencyId.GGP,
			CurrencyId.TVD,
			CurrencyId.IMP,
			CurrencyId.SPL
	));

	private CurrencyId randomCurrency(Random rnd) {
		while (true) {
			CurrencyId currencyId = CurrencyId.values()[rnd.nextInt(CurrencyId.values().length)];
			if (VALID_CURRENCIES.contains(currencyId)) {
				return currencyId;
			}
		}
	}

	@Test
	public void itShouldBeReflective() throws Exception {
		Random rnd = new Random();
		for (int i = 0; i < 1000; i++) {
			long amount = rnd.nextInt();
			LanguageId languageId = LanguageId.values()[rnd.nextInt(LanguageId.values().length)];
			CountryId countryId = CountryId.values()[rnd.nextInt(CountryId.values().length)];
			CurrencyId currencyId = randomCurrency(rnd);
			Locale locale = new Locale(languageId.name(), countryId.name());
			String hrString = format(currencyId, locale, amount);
			long parsedAmount = parse(currencyId, locale, hrString);
			assertEquals(currencyId + " with a " + locale + " locale was not reflexive: " + hrString, amount, parsedAmount);
		}
	}

	@Test
	public void itShouldFormatFractionizedAmount() throws Exception {
		assertEquals("100,00 kr", format(SEK, LOCALE_SE, 10000));
		assertEquals("0,00 kr", format(SEK, LOCALE_SE, -0));
		assertEquals("-1,00 kr", format(SEK, LOCALE_SE, -100));
		assertEquals("-100 000,00 kr", format(SEK, LOCALE_SE, -10000000));
		assertEquals("-100 000,25 kr", format(SEK, LOCALE_SE, -10000025));
		assertEquals("100 000,25 kr", format(SEK, LOCALE_SE, 10000025));
		assertEquals("0,25 kr", format(SEK, LOCALE_SE, 25));
		assertEquals("-0,25 kr", format(SEK, LOCALE_SE, -25));
		assertEquals("100 000,25 €", format(EUR, LOCALE_SE, 10000025));
		assertEquals("€0.00", format(EUR, Locale.UK, 0));
		assertEquals("EUR0.00", format(EUR, Locale.US, -0));
		assertEquals("(EUR1.00)", format(EUR, Locale.US, -100));
		assertEquals("-€100,000.00", format(EUR, Locale.UK, -10000000));
		assertEquals("-€100,000.25", format(EUR, Locale.UK, -10000025));
		assertEquals("EUR0.25", format(EUR, Locale.US, 25));
		assertEquals("(EUR0.25)", format(EUR, Locale.US, -25));
	}

	@Test
	public void itShouldConvertToOre() throws Exception {
		CurrencyId currencyId = SEK;
		assertEquals(1000000, parse(currencyId, "10000"));
		assertEquals(1000000, parse(currencyId, "10000.00"));
		assertEquals(0, parse(currencyId, "0.00"));
		assertEquals(0, parse(currencyId, "0"));
		assertEquals(0, parse(currencyId, "-0.00"));
		assertEquals(-100, parse(currencyId, "-1.00"));
		assertEquals(-10000000, parse(currencyId, "-100000.00"));
		assertEquals(-10000025, parse(currencyId, "-100000.25"));
		assertEquals(10000025, parse(currencyId, "100000.25"));
		assertEquals(10000026, parse(currencyId, "100000.256"));
		assertEquals(25, parse(currencyId, "0.25"));
		assertEquals(25, parse(currencyId, "0.250"));
		assertEquals(-25, parse(currencyId, "-0.25"));
	}

	@Test
	public void itShouldConvertEuroCents() throws Exception {
		CurrencyId currencyId = EUR;
		assertEquals(1000000, parse(currencyId, "10000"));
		assertEquals(1000000, parse(currencyId, "10000.00"));
		assertEquals(0, parse(currencyId, "0.00"));
		assertEquals(0, parse(currencyId, "0"));
		assertEquals(0, parse(currencyId, "-0.00"));
		assertEquals(-100, parse(currencyId, "-1.00"));
		assertEquals(-10000000, parse(currencyId, "-100000.00"));
		assertEquals(-10000025, parse(currencyId, "-100000.25"));
		assertEquals(10000025, parse(currencyId, "100000.25"));
		assertEquals(25, parse(currencyId, "0.25"));
		assertEquals(25, parse(currencyId, "0.250"));
		assertEquals(-25, parse(currencyId, "-0.25"));
	}

	@Test
	public void itShouldConvertUSCents() throws Exception {
		CurrencyId currencyId = USD;
		assertEquals(1000000, parse(currencyId, "10000"));
		assertEquals(1000000, parse(currencyId, "10000.00"));
		assertEquals(0, parse(currencyId, "0.00"));
		assertEquals(0, parse(currencyId, "0"));
		assertEquals(0, parse(currencyId, "-0.00"));
		assertEquals(-100, parse(currencyId, "-1.00"));
		assertEquals(-10000000, parse(currencyId, "-100000.00"));
		assertEquals(-10000025, parse(currencyId, "-100000.25"));
		assertEquals(10000025, parse(currencyId, "100000.25"));
		assertEquals(25, parse(currencyId, "0.25"));
		assertEquals(25, parse(currencyId, "0.250"));
		assertEquals(-25, parse(currencyId, "-0.25"));
	}

	@Test
	public void itShouldConvertNZCents() throws Exception {
		CurrencyId currencyId = NZD;
		assertEquals(1000000, parse(currencyId, "10000"));
		assertEquals(1000000, parse(currencyId, "10000.00"));
		assertEquals(0, parse(currencyId, "0.00"));
		assertEquals(0, parse(currencyId, "0"));
		assertEquals(0, parse(currencyId, "-0.00"));
		assertEquals(-100, parse(currencyId, "-1.00"));
		assertEquals(-10000000, parse(currencyId, "-100000.00"));
		assertEquals(-10000025, parse(currencyId, "-100000.25"));
		assertEquals(10000025, parse(currencyId, "100000.25"));
		assertEquals(25, parse(currencyId, "0.25"));
		assertEquals(25, parse(currencyId, "0.250"));
		assertEquals(-25, parse(currencyId, "-0.25"));
	}

	@Test
	public void itShouldConvertIraqiDinars() throws Exception {
		CurrencyId currencyId = IQD;
		assertEquals(10000000, parse(currencyId, "10000"));
		assertEquals(10000000, parse(currencyId, "10000.000"));
		assertEquals(0, parse(currencyId, "0.000"));
		assertEquals(0, parse(currencyId, "0"));
		assertEquals(0, parse(currencyId, "-0.000"));
		assertEquals(-1000, parse(currencyId, "-1.000"));
		assertEquals(-100000000, parse(currencyId, "-100000.000"));
		assertEquals(-100000250, parse(currencyId, "-100000.250"));
		assertEquals(100000250, parse(currencyId, "100000.250"));
		assertEquals(250, parse(currencyId, "0.25"));
		assertEquals(250, parse(currencyId, "0.250"));
		assertEquals(-250, parse(currencyId, "-0.250"));
	}
}
