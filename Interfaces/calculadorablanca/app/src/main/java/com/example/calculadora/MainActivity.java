package com.example.calculadora;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.calculadora.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    
    private double memory = 0;
    private boolean isNewInput = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar los listeners numéricos
        View.OnClickListener numListener = v -> {
            Button b = (Button) v;
            insertNumber(b.getText().toString());
        };

        int[] numIds = {R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, 
                        R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9};
        for (int id : numIds) {
            findViewById(id).setOnClickListener(numListener);
        }

        // Listener del punto decimal
        binding.btnDot.setOnClickListener(v -> {
            if (isNewInput) {
                binding.etScreen.setText("0.");
                isNewInput = false;
                return;
            }
            
            String currentText = binding.etScreen.getText().toString();
            if (currentText.endsWith(" ")) {
                binding.etScreen.append("0.");
            } else {
                String[] tokens = currentText.split("\\s+");
                if (tokens.length > 0) {
                    String lastToken = tokens[tokens.length - 1];
                    if (!lastToken.contains(".")) {
                        binding.etScreen.append(".");
                    }
                } else {
                    binding.etScreen.setText("0.");
                }
            }
        });

        // Listeners de operadores básicos
        View.OnClickListener opListener = v -> {
            Button b = (Button) v;
            setOperator(b.getText().toString());
        };
        binding.btnAdd.setOnClickListener(opListener);
        binding.btnSub.setOnClickListener(opListener);
        binding.btnMult.setOnClickListener(opListener);
        binding.btnDiv.setOnClickListener(opListener);

        // Operaciones especiales
        binding.btnPow.setOnClickListener(v -> setOperator("^"));
        binding.btnSqrt.setOnClickListener(v -> calculateSqrt());

        // Memoria M+ y M-
        binding.btnMPlus.setOnClickListener(v -> {
            try {
                String currentText = binding.etScreen.getText().toString();
                String evaluated = evaluateExpression(currentText);
                if (evaluated.isEmpty()) {
                    evaluated = currentText;
                }
                double currentVal = Double.parseDouble(evaluated);
                memory += currentVal;
                Toast.makeText(this, "Guardado en Memoria: " + formatResult(memory), Toast.LENGTH_SHORT).show();
                isNewInput = true;
            } catch (NumberFormatException e) {
                // Ignore
            }
        });

        binding.btnMMinus.setOnClickListener(v -> {
            String memoryStr = formatResult(memory);
            insertNumber(memoryStr);
        });

        // Controles C, Borrar y =
        binding.btnC.setOnClickListener(v -> {
            binding.etScreen.setText("0");
            binding.tvHistory.setText("");
            isNewInput = true;
        });

        binding.btnBack.setOnClickListener(v -> {
            if (isNewInput) {
                binding.etScreen.setText("0");
                binding.tvHistory.setText("");
                return;
            }
            
            String currentText = binding.etScreen.getText().toString();
            if (currentText.equals("Error") || currentText.equals("Error (Div/0)") || currentText.length() <= 1) {
                binding.etScreen.setText("0");
                binding.tvHistory.setText("");
                isNewInput = true;
            } else {
                if (currentText.endsWith(" ")) {
                    String trimmed = currentText.trim();
                    int lastSpace = trimmed.lastIndexOf(" ");
                    if (lastSpace != -1) {
                        binding.etScreen.setText(trimmed.substring(0, lastSpace));
                    } else {
                        binding.etScreen.setText("0");
                        isNewInput = true;
                    }
                } else {
                    binding.etScreen.setText(currentText.substring(0, currentText.length() - 1));
                }
            }
        });

        binding.btnEquals.setOnClickListener(v -> calculateResult());

        // TextWatcher para el resultado en tiempo real (Live Preview)
        binding.etScreen.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                
                if (isNewInput) {
                    return;
                }
                
                boolean hasOperator = false;
                for (String op : new String[]{"+", "-", "X", "/", "^"}) {
                    if (text.contains(op)) {
                        hasOperator = true;
                        break;
                    }
                }
                
                if (hasOperator) {
                    String preview = evaluateExpression(text);
                    if (!preview.isEmpty() && !preview.equals("Error") && !preview.equals("Error (Div/0)")) {
                        binding.tvHistory.setText(preview);
                    } else {
                        binding.tvHistory.setText("");
                    }
                } else {
                    binding.tvHistory.setText("");
                }
            }
        });
    }

    private void insertNumber(String num) {
        if (isNewInput) {
            binding.etScreen.setText(num);
            isNewInput = false;
            binding.tvHistory.setText("");
        } else {
            String currentText = binding.etScreen.getText().toString();
            if (currentText.equals("0")) {
                binding.etScreen.setText(num);
            } else {
                binding.etScreen.append(num);
            }
        }
    }

    private void setOperator(String op) {
        String currentText = binding.etScreen.getText().toString();
        if (currentText.equals("Error") || currentText.equals("Error (Div/0)")) {
            return;
        }
        
        if (isNewInput) {
            isNewInput = false;
        }
        
        if (currentText.endsWith(" ")) {
            String trimmed = currentText.trim();
            int lastSpace = trimmed.lastIndexOf(" ");
            if (lastSpace != -1) {
                binding.etScreen.setText(trimmed.substring(0, lastSpace) + " " + op + " ");
            } else {
                binding.etScreen.setText(trimmed + " " + op + " ");
            }
        } else {
            binding.etScreen.append(" " + op + " ");
        }
    }

    private void calculateSqrt() {
        String currentText = binding.etScreen.getText().toString();
        if (currentText.equals("Error") || currentText.equals("Error (Div/0)")) {
            return;
        }
        
        String evaluated = evaluateExpression(currentText);
        if (evaluated.isEmpty()) {
            evaluated = currentText;
        }
        
        try {
            double val = Double.parseDouble(evaluated);
            if (val >= 0) {
                double result = Math.sqrt(val);
                binding.tvHistory.setText("√(" + formatResult(val) + ") =");
                isNewInput = true;
                binding.etScreen.setText(formatResult(result));
            } else {
                binding.tvHistory.setText("√(" + formatResult(val) + ") =");
                isNewInput = true;
                binding.etScreen.setText("Error");
            }
        } catch (NumberFormatException e) {
            // Ignorar
        }
    }

    private void calculateResult() {
        String currentText = binding.etScreen.getText().toString();
        if (currentText.equals("Error") || currentText.equals("Error (Div/0)")) {
            return;
        }
        
        boolean hasOperator = false;
        for (String op : new String[]{"+", "-", "X", "/", "^"}) {
            if (currentText.contains(op)) {
                hasOperator = true;
                break;
            }
        }
        
        if (!hasOperator) return;
        
        if (currentText.endsWith(" ")) {
            currentText = currentText.trim();
            int lastSpace = currentText.lastIndexOf(" ");
            if (lastSpace != -1) {
                currentText = currentText.substring(0, lastSpace).trim();
            } else {
                return;
            }
        }
        
        String result = evaluateExpression(currentText);
        isNewInput = true;
        if (result.isEmpty()) {
            binding.etScreen.setText("Error");
            binding.tvHistory.setText("");
        } else {
            binding.tvHistory.setText(currentText + " =");
            binding.etScreen.setText(result);
        }
    }

    private String evaluateExpression(String expr) {
        if (expr == null || expr.trim().isEmpty()) {
            return "";
        }
        
        String[] tokensArray = expr.trim().split("\\s+");
        if (tokensArray.length == 0) {
            return "";
        }
        
        List<String> tokens = new ArrayList<>(Arrays.asList(tokensArray));
        
        if (tokens.size() > 0) {
            String lastToken = tokens.get(tokens.size() - 1);
            if (isOperator(lastToken)) {
                tokens.remove(tokens.size() - 1);
            }
        }
        
        if (tokens.isEmpty()) {
            return "";
        }
        
        try {
            // Fase 1: Potencias "^"
            for (int i = 0; i < tokens.size(); i++) {
                if (tokens.get(i).equals("^")) {
                    if (i > 0 && i < tokens.size() - 1) {
                        double base = Double.parseDouble(tokens.get(i - 1));
                        double exponent = Double.parseDouble(tokens.get(i + 1));
                        double res = Math.pow(base, exponent);
                        tokens.set(i - 1, String.valueOf(res));
                        tokens.remove(i);
                        tokens.remove(i);
                        i--;
                    } else {
                        return "";
                    }
                }
            }
            
            // Fase 2: Multiplicación "X" y División "/"
            for (int i = 0; i < tokens.size(); i++) {
                String token = tokens.get(i);
                if (token.equals("X") || token.equals("/")) {
                    if (i > 0 && i < tokens.size() - 1) {
                        double left = Double.parseDouble(tokens.get(i - 1));
                        double right = Double.parseDouble(tokens.get(i + 1));
                        double res = 0;
                        if (token.equals("X")) {
                            res = left * right;
                        } else {
                            if (right == 0) {
                                return "Error (Div/0)";
                            }
                            res = left / right;
                        }
                        tokens.set(i - 1, String.valueOf(res));
                        tokens.remove(i);
                        tokens.remove(i);
                        i--;
                    } else {
                        return "";
                    }
                }
            }
            
            // Fase 3: Suma "+" y Resta "-"
            for (int i = 0; i < tokens.size(); i++) {
                String token = tokens.get(i);
                if (token.equals("+") || token.equals("-")) {
                    if (i > 0 && i < tokens.size() - 1) {
                        double left = Double.parseDouble(tokens.get(i - 1));
                        double right = Double.parseDouble(tokens.get(i + 1));
                        double res = 0;
                        if (token.equals("+")) {
                            res = left + right;
                        } else {
                            res = left - right;
                        }
                        tokens.set(i - 1, String.valueOf(res));
                        tokens.remove(i);
                        tokens.remove(i);
                        i--;
                    } else {
                        return "";
                    }
                }
            }
            
            if (tokens.size() == 1) {
                double finalVal = Double.parseDouble(tokens.get(0));
                return formatResult(finalVal);
            }
        } catch (Exception e) {
            return "";
        }
        
        return "";
    }
    
    private boolean isOperator(String token) {
        return token.equals("+") || token.equals("-") || token.equals("X") || token.equals("/") || token.equals("^");
    }

    private String formatResult(double result) {
        if (result == (long) result) {
            return String.format("%d", (long) result);
        } else {
            return String.format("%s", result);
        }
    }
}
