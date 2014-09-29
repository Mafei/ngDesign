-------------------------------------------------------------------------------
-- Copyright (c) 2012-2013 Synflow SAS.
-- All rights reserved. This program and the accompanying materials
-- are made available under the terms of the Eclipse Public License v1.0
-- which accompanies this distribution, and is available at
-- http://www.eclipse.org/legal/epl-v10.html
--
-- Contributors:
--    Nicolas Siret - initial API and implementation and/or initial documentation
--    Matthieu Wipliez - refactoring and maintenance
-------------------------------------------------------------------------------

-------------------------------------------------------------------------------
-- Title      : Simple register
-- Author     : Nicolas Siret (nicolas.siret@synflow.com)
--              Matthieu Wipliez (matthieu.wipliez@synflow.com)
-- Standard   : VHDL'93
-------------------------------------------------------------------------------

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

-------------------------------------------------------------------------------


entity Simple_Register is
  generic (
    depth : integer := 8);
  port (
    reset_n : in  std_logic;
    clock   : in  std_logic;
    din     : in  unsigned(depth - 1 downto 0);
    dout    : out unsigned(depth - 1 downto 0)
    );
end Simple_Register;

-------------------------------------------------------------------------------

architecture arch_Simple_Register of Simple_Register is

  -----------------------------------------------------------------------------
  -- Internal type and signal declaration
  -----------------------------------------------------------------------------

  signal reg_value : unsigned(depth - 1 downto 0);
  -----------------------------------------------------------------------------

begin

  syncProcess : process(reset_n, clock)
  begin
    if reset_n = '0' then
      reg_value <= (others => '0');
      dout      <= (others => '0');
    elsif rising_edge(clock) then
      reg_value <= din;
      dout      <= reg_value;
    end if;
  end process syncProcess;

end arch_Simple_Register;