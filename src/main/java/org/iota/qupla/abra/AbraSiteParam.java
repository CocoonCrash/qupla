package org.iota.qupla.abra;

import org.iota.qupla.abra.context.AbraCodeContext;
import org.iota.qupla.context.CodeContext;

public class AbraSiteParam extends AbraSite
{
  public int offset;

  @Override
  public CodeContext append(final CodeContext context)
  {
    return super.append(context).append("param " + name + "[" + size + "]");
  }

  @Override
  public void eval(final AbraCodeContext context)
  {
    context.evalParam(this);
  }
}
