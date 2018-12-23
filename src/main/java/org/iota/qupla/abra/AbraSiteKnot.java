package org.iota.qupla.abra;

import java.util.ArrayList;
import java.util.HashMap;

import org.iota.qupla.abra.funcs.ConcatManager;
import org.iota.qupla.abra.funcs.NullifyManager;
import org.iota.qupla.abra.funcs.SliceManager;
import org.iota.qupla.context.AbraContext;
import org.iota.qupla.context.CodeContext;

public class AbraSiteKnot extends AbraSite
{
  public static ConcatManager concats = new ConcatManager();
  public static NullifyManager nullifyFalse = new NullifyManager(false);
  public static NullifyManager nullifyTrue = new NullifyManager(true);
  public static SliceManager slicers = new SliceManager();
  public static int vectorReuse;
  public static HashMap<String, AbraBlockBranch> vectors = new HashMap<>();
  public AbraBlock block;
  public ArrayList<AbraSite> inputs = new ArrayList<>();

  @Override
  public CodeContext append(final CodeContext context)
  {
    super.append(context).append("knot(");
    boolean first = true;
    for (AbraSite input : inputs)
    {
      context.append(first ? "" : ", ").append("" + refer(input.index));
      first = false;
    }

    return context.append(") " + block);
  }

  public void branch(final AbraContext context)
  {
    for (final AbraBlock branch : context.abra.branches)
    {
      if (branch.name.equals(name))
      {
        block = branch;
        break;
      }
    }
  }

  @Override
  public void code(final TritCode tritCode)
  {
    tritCode.putTrit('-');
    tritCode.putInt(inputs.size());
    for (final AbraSite input : inputs)
    {
      tritCode.putInt(refer(input.index));
    }

    tritCode.putInt(block.index);
  }

  public void concat(final AbraContext context)
  {
    block = concats.find(context, size);
  }

  public void lut(final AbraContext context)
  {
    for (final AbraBlock lut : context.abra.luts)
    {
      if (lut.name.equals(name))
      {
        block = lut;
        break;
      }
    }
  }

  public void nullifyFalse(final AbraContext context)
  {
    block = nullifyFalse.find(context, size);
  }

  public void nullifyTrue(final AbraContext context)
  {
    block = nullifyTrue.find(context, size);
  }

  public void slice(final AbraContext context, final int inputSize, final int start)
  {
    block = slicers.find(context, inputSize, start, size);
  }

  public void vector(final AbraContext context, final String trits)
  {
    final String branchName = "const$" + size + "$" + name;
    block = vectors.get(branchName);
    if (block != null)
    {
      vectorReuse++;
      return;
    }

    // create branch that has 1 input of 1 trit
    final AbraBlockBranch branch = new AbraBlockBranch();
    branch.size = size;
    branch.name = branchName;

    final AbraSiteParam param = new AbraSiteParam();
    param.size = 1;
    param.name = "dummy";
    branch.inputs.add(param);

    final AbraSiteKnot siteMin = vectorTritLut(context, branch, "constMin$0");
    final AbraSiteKnot siteOne = vectorTritLut(context, branch, "constOne$0");
    final AbraSiteKnot siteZero = vectorTritLut(context, branch, "constZero$0");

    final AbraSiteKnot constant = new AbraSiteKnot();
    constant.size = branch.size;
    for (int i = 0; i < branch.size; i++)
    {
      final char c = trits.charAt(i);
      constant.inputs.add(c == '0' ? siteZero : c == '1' ? siteOne : siteMin);
    }

    constant.concat(context);
    branch.outputs.add(constant);

    context.abra.branches.add(branch);
    vectors.put(branchName, branch);
    block = branch;
  }

  public AbraSiteKnot vectorTritLut(final AbraContext context, final AbraBlockBranch branch, final String lutName)
  {
    final AbraSite input = branch.inputs.get(0);

    final AbraSiteKnot site = new AbraSiteKnot();
    site.size = 3;
    site.name = lutName;
    site.inputs.add(input);
    site.inputs.add(input);
    site.inputs.add(input);
    site.lut(context);
    branch.sites.add(site);
    return site;
  }
}
