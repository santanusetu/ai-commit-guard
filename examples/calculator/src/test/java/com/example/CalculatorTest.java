import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class CalculatorTest {

@Test
void testPowerOperation() {
Calculator calculator = new Calculator();
double result = calculator.calculate("power", 2, 3);
assertEquals(8.0, result);
}

@Test
void testInvalidOperation() {
Calculator calculator = new Calculator();
Exception exception = assertThrows(IllegalArgumentException.class, () -> {
calculator.calculate("invalid", 2, 3);
});
assertEquals("Unsupported operation: invalid", exception.getMessage());
}
}