package com.velaphi.mealplan

import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    BarcodeScannerTest::class,
    MealPlanScreenTest::class
)
class MealPlanTestSuite {
    // This class serves as a test suite to run all mealplan tests together
}
